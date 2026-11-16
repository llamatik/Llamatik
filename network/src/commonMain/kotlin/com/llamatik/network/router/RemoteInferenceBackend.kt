package com.llamatik.network.router

import com.llamatik.core.platform.GenStream
import com.llamatik.core.platform.InferenceBackend
import com.llamatik.network.client.LlamatikServerClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Streams from a remote Llamatik server. Dropped connections are resumed transparently by
 * [LlamatikServerClient]; [GenStream.onError] is only called once reconnecting has failed or the
 * server cannot resume the stream any more (a [GenStream] cannot discard already delivered text,
 * so restarting is left to the caller).
 */
class RemoteInferenceBackend(
    private val client: LlamatikServerClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : InferenceBackend {

    override fun generateStream(prompt: String, stream: GenStream) {
        scope.launch {
            try {
                client.streamGenerate(prompt).collect { token -> stream.onDelta(token) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                stream.onError(e.message ?: "Remote inference failed")
                return@launch
            }
            stream.onComplete()
        }
    }
}
