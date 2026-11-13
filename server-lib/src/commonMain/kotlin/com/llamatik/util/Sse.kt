package com.llamatik.util

import com.llamatik.core.platform.GenStream
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.serialization.json.JsonPrimitive

/**
 * Minimal utilities for Server-Sent Events (SSE) streaming.
 *
 * We emit JSON lines as SSE data events:
 *   data: {...}
 *
 * The client can reassemble deltas until it receives a "done" event.
 */
object Sse {

    fun genStreamCallback(
        onDelta: (String) -> Boolean,
        onDone: () -> Boolean,
        onError: (String) -> Boolean
    ): GenStream = object : GenStream {
        override fun onDelta(text: String) {
            onDelta(text)
        }

        override fun onComplete() {
            onDone()
        }

        override fun onError(message: String) {
            onError(message)
        }
    }

    suspend fun pipe(
        writer: ByteWriteChannel,
        deltas: Channel<String>,
        done: Channel<Unit>,
        errors: Channel<String>
    ) {
        suspend fun ByteWriteChannel.writeEvent(json: String) {
            writeStringUtf8("data: $json\n\n")
            flush()
        }

        while (true) {
            val outcome = select<String?> {
                deltas.onReceiveCatching { it.getOrNull() }
                errors.onReceiveCatching { err ->
                    val msg = err.getOrNull()
                    if (msg != null) "{\"event\":\"error\",\"message\":${jsonString(msg)}}" else null
                }
                done.onReceiveCatching {
                    "{\"event\":\"done\"}"
                }
            }

            if (outcome == null) break

            if (outcome.startsWith("{\"event\":\"error\"")) {
                writer.writeEvent(outcome)
                break
            }

            if (outcome == "{\"event\":\"done\"}") {
                writer.writeEvent(outcome)
                break
            }

            // delta
            writer.writeEvent("{\"event\":\"delta\",\"text\":${jsonString(outcome)}}")
        }

        deltas.close()
        done.close()
        errors.close()
    }

    private fun jsonString(value: String): String {
        // Delegate escaping to kotlinx.serialization so all JSON control characters stay valid in SSE payloads.
        return JsonPrimitive(value).toString()
    }
}
