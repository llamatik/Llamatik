# Resumable server streaming: reconnect and continue generation after connection drops

## Summary

When a client was streaming a generation from a Llamatik server and the connection dropped (or the request ran longer than 30s), the app immediately suggested switching to the local model. This PR makes streaming robust: dropped connections are resumed transparently and the generation continues where it left off. The local-model fallback is only offered once reconnecting has failed several times or the server is gone.

## Root causes

- `ChatBotViewModel` wrapped the **whole** server generation in `withTimeoutOrNull(30_000)`, so any answer taking more than 30s was cut off and the local fallback was suggested.
- `parseSse` treated a closed connection as a normal end of stream, so a truncated response looked complete.
- There was no retry: the first exception went straight to the fallback dialog.
- Generations were tied to the HTTP request, so a dropped connection killed them, and the server had no way to resume.

## Changes

### Server (`server-lib`)
- New `StreamSessions` registry: generations run in a server-owned scope, not inside the request, and every event is recorded in a replay log.
- Resume protocol on the existing streaming endpoints (`stream`, `streamWithContext`, `continueStream`, `jsonStream`, `jsonStreamWithContext`, `multimodal/analyzeStream`):
  - Client sends `X-Stream-Id`; events are numbered with SSE `id:` lines.
  - Repeating the same id with `Last-Event-ID: n` reattaches, replays events after `n` and follows the live output (no regeneration).
  - `410 Gone` if the stream is unknown, expired, or the offset is past the end.
  - Responses carry `X-Stream-Resume: 1`.
- Unattended generations are cancelled after 90s; finished streams stay resumable for 5 minutes; a new request cancels unattended ones (the model runs one generation at a time).
- Requests without `X-Stream-Id` keep the old behavior (generation dies with its connection).

### Client (`network`)
- All `LlamatikServerClient` streaming calls go through one resumable helper:
  - Up to 4 reconnects with 1s/2s/4s/8s backoff; the counter resets whenever a retry makes progress.
  - 120s without data is treated as a dead connection.
  - Streams that end without the server's `done` event now throw `StreamInterruptedException` instead of being treated as complete.
  - Server-reported errors and 4xx responses are not retried (`RemoteInferenceException`).
  - If the server cannot resume (410, or an older server without the resume header), `StreamExpiredException` is thrown rather than silently restarting and duplicating text.
- New `StreamRetryPolicy` constructor parameter to configure retries.
- `RemoteInferenceBackend` benefits from the same retry/resume and only calls `onError` once retries are exhausted or the stream expired.

### App (`shared`)
- Removed the 30s whole-generation timeout.
- `ChatBotViewModel` restarts the request (replacing partial text) only on `StreamExpiredException`, and only if the server is still reachable with a model loaded. Any other failure means the server is gone, so the local-model fallback is offered.

## Compatibility

- Old clients with a new server and new clients with an old server both work: without the resume header the client refuses to resume and reports the stream as expired, so output is never duplicated.
- Image/video generation are single request/response calls and are unchanged.

## Testing

- `StreamResumeTest` (server, 6 tests): event ids, replay of only missed events, resume of a finished stream, 410 for unknown/too-far offsets, idempotent re-send of the first request.
- `ResumableStreamTest` (client, 7 tests, new `ktor-client-mock` test dependency): resume without duplication, retry limit, counter reset on progress, 410 and no-resume-support handling, no retry on server errors / 4xx.
- `:network:jvmTest`, `:server-lib:jvmTest` pass; `:shared` compiles for JVM and Android; `:server-lib` and `:network` compile for iOS simulator, and `:network` for wasm.

## Not covered / follow-ups

- Not yet verified end to end against a real server over a real network drop (Wi-Fi/cellular switch, server restart).
- When the server cannot resume, the request restarts from scratch (partial text is replaced), since a stateless server cannot continue an expired generation.
- `RemoteInferenceBackend` cannot restart on expiry because `GenStream` has no way to discard already delivered text.
