package com.llamatik.util

import com.llamatik.core.platform.GenStream
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive

/** Request header carrying the client-chosen id of a stream; re-sending it attaches to the same stream. */
const val STREAM_ID_HEADER = "X-Stream-Id"

/** Standard SSE header: number of events the client already received. */
const val LAST_EVENT_ID_HEADER = "Last-Event-ID"

/** Response header set to `1` when the server supports resuming this stream. */
const val STREAM_RESUME_HEADER = "X-Stream-Resume"

/** One entry of a stream's replay log. The last entry of a finished stream is [Done] or [Error]. */
internal sealed interface StreamEvent {
    data class Delta(val text: String) : StreamEvent
    data object Done : StreamEvent
    data class Error(val message: String) : StreamEvent

    val isTerminal: Boolean get() = this !is Delta
}

/**
 * A generation whose output is recorded server-side so a client that lost its connection can
 * reattach and receive the missed events, then keep following the live output.
 *
 * Event ids are 1-based positions in the log; a client that has seen `n` events resumes with
 * `Last-Event-ID: n`.
 */
internal class StreamSession(
    val id: String,
    private val scope: CoroutineScope,
    private val resumable: Boolean,
    private val onAbandon: suspend () -> Unit,
    private val onFinished: (StreamSession) -> Unit,
) {
    private val inbox = Channel<StreamEvent>(Channel.UNLIMITED)
    private val mutex = Mutex()
    private val log = ArrayList<StreamEvent>()
    private val version = MutableStateFlow(0)
    private var subscribers = 0
    private var finished = false
    private var generationJob: Job? = null
    private var watchdog: Job? = null

    val callback: GenStream = object : GenStream {
        override fun onDelta(text: String) {
            inbox.trySend(StreamEvent.Delta(text))
        }

        override fun onComplete() {
            inbox.trySend(StreamEvent.Done)
        }

        override fun onError(message: String) {
            inbox.trySend(StreamEvent.Error(message))
        }
    }

    /** True while the generation is running and nobody is listening. */
    suspend fun isOrphan(): Boolean = mutex.withLock { !finished && subscribers == 0 }

    internal fun launch(run: (GenStream) -> Unit) {
        scope.launch { pump() }
        generationJob = scope.launch(blockingDispatcher) {
            try {
                run(callback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                inbox.trySend(StreamEvent.Error(e.message ?: "Streaming failed"))
            }
        }
    }

    private suspend fun pump() {
        for (event in inbox) {
            val last = mutex.withLock {
                if (finished) return@withLock true
                log.add(event)
                if (event.isTerminal) finished = true
                finished
            }
            version.value = version.value + 1
            if (last) break
        }
        inbox.close()
        onFinished(this)
    }

    /** Stops an unattended generation: cancels the native work and terminates the log. */
    suspend fun abandon(reason: String) {
        val wasRunning = mutex.withLock { !finished }
        if (!wasRunning) return
        runCatching { onAbandon() }
        generationJob?.cancel()
        inbox.trySend(StreamEvent.Error(reason))
    }

    /**
     * Writes events from position [from] to [writer] as SSE, following the live output until the
     * stream ends. Returns normally when a terminal event was written.
     */
    suspend fun pipe(from: Int, writer: ByteWriteChannel) {
        mutex.withLock {
            subscribers++
            watchdog?.cancel()
            watchdog = null
        }
        try {
            var index = from
            while (true) {
                val seen = version.value
                val batch = mutex.withLock { if (index < log.size) log.subList(index, log.size).toList() else emptyList() }
                for (event in batch) {
                    index++
                    writer.writeStringUtf8("id: $index\ndata: ${event.toJson()}\n\n")
                    if (event.isTerminal) {
                        writer.flush()
                        return
                    }
                }
                if (batch.isNotEmpty()) writer.flush()
                // Wake up when the pump appended something after our snapshot.
                version.first { it != seen }
            }
        } finally {
            withContext(NonCancellable) {
                mutex.withLock {
                    subscribers--
                    if (subscribers == 0 && !finished) {
                        watchdog = scope.launch {
                            if (resumable) delay(StreamSessions.ABANDON_AFTER_MS)
                            abandon("Client did not reconnect")
                        }
                    }
                }
            }
        }
    }

    /** Number of events in the log (used to validate resume offsets). */
    suspend fun size(): Int = mutex.withLock { log.size }

    private fun StreamEvent.toJson(): String = when (this) {
        is StreamEvent.Delta -> "{\"event\":\"delta\",\"text\":${JsonPrimitive(text)}}"
        StreamEvent.Done -> "{\"event\":\"done\"}"
        is StreamEvent.Error -> "{\"event\":\"error\",\"message\":${JsonPrimitive(message)}}"
    }
}

internal sealed interface Attach {
    data class Session(val session: StreamSession, val from: Int) : Attach

    /** The client asked to resume a stream that does not exist (anymore). */
    data object Gone : Attach
}

/**
 * Registry of recent streams. Generations run in this registry's own scope, not in the HTTP
 * request, so a dropped connection does not kill them.
 */
internal object StreamSessions {
    /** How long an unattended generation keeps running before it is cancelled. */
    var ABANDON_AFTER_MS = 90_000L

    /** How long a finished stream stays resumable. */
    var RETAIN_FINISHED_MS = 5 * 60_000L

    private val scope = CoroutineScope(SupervisorJob())
    private val lock = Mutex()
    private val sessions = HashMap<String, StreamSession>()

    /**
     * Attaches to stream [streamId] if it exists (replaying from [lastEventId]), otherwise starts
     * a new generation with [run]. A null [streamId] (legacy client) starts a stream that is
     * cancelled as soon as its connection drops, like before resume support existed.
     */
    suspend fun attachOrStart(
        streamId: String?,
        lastEventId: Int,
        onAbandon: suspend () -> Unit,
        run: (GenStream) -> Unit,
    ): Attach {
        val orphans: List<StreamSession>
        val session: StreamSession
        lock.withLock {
            val existing = streamId?.let { sessions[it] }
            if (existing != null) {
                if (lastEventId > existing.size()) return Attach.Gone
                return Attach.Session(existing, lastEventId.coerceAtLeast(0))
            }
            if (lastEventId > 0) return Attach.Gone

            // The model runs one generation at a time: a new request supersedes unattended ones.
            orphans = sessions.values.filter { it.isOrphan() }
            val id = streamId ?: randomId()
            session = StreamSession(id, scope, resumable = streamId != null, onAbandon) { done -> scheduleRemoval(done) }
            sessions[id] = session
        }
        orphans.forEach { it.abandon("Superseded by a new request") }
        session.launch(run)
        return Attach.Session(session, 0)
    }

    private fun scheduleRemoval(session: StreamSession) {
        scope.launch {
            delay(RETAIN_FINISHED_MS)
            lock.withLock { if (sessions[session.id] === session) sessions.remove(session.id) }
        }
    }

    private fun randomId(): String =
        buildString { repeat(2) { append(kotlin.random.Random.nextLong().toULong().toString(16)) } }
}

/**
 * Responds to a streaming request with a resumable SSE stream.
 *
 * - No [STREAM_ID_HEADER]: legacy behaviour, a new stream that dies with its connection.
 * - With it: the first request starts the generation; later requests with the same id (e.g. after
 *   a network drop) reattach and replay everything after `Last-Event-ID`. 410 if the stream is
 *   unknown or expired.
 */
internal suspend fun ApplicationCall.respondResumableStream(
    onAbandon: suspend () -> Unit,
    run: (GenStream) -> Unit,
) {
    val streamId = request.headers[STREAM_ID_HEADER]?.takeIf { it.isNotBlank() && it.length <= 128 }
    val lastEventId = request.headers[LAST_EVENT_ID_HEADER]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    when (val attach = StreamSessions.attachOrStart(streamId, lastEventId, onAbandon, run)) {
        Attach.Gone -> respond(HttpStatusCode.Gone, "Stream expired")
        is Attach.Session -> {
            response.headers.append(STREAM_ID_HEADER, attach.session.id)
            response.headers.append(STREAM_RESUME_HEADER, "1")
            respondBytesWriter(contentType = ContentType.Text.EventStream) {
                attach.session.pipe(attach.from, this)
            }
        }
    }
}
