package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.AnalyzeImageRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.api.MultimodalInitRequest
import com.llamatik.api.OkResponse
import com.llamatik.core.platform.GenStream
import com.llamatik.multimodal.MultimodalService
import com.llamatik.multimodal.MultimodalServicePort
import com.llamatik.status.ModelRegistry
import com.llamatik.util.PathValidator
import com.llamatik.util.Sse
import com.llamatik.util.sanitizedMessage
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import java.util.Base64

private const val VLM = "$API_VERSION/multimodal"
private const val VLM_INIT = "$VLM/init"
private const val VLM_ANALYZE_STREAM = "$VLM/analyzeStream"
private const val VLM_CANCEL = "$VLM/cancel"
private const val VLM_RELEASE = "$VLM/release"

@Suppress("TooGenericExceptionCaught")
fun Route.multimodalRoutes(service: MultimodalServicePort = MultimodalService) {
    post(VLM_INIT) {
        val req = call.receive<MultimodalInitRequest>()
        // Throws IllegalArgumentException on invalid paths — caught by StatusPages → 400
        val validatedModel = PathValidator.validateModelPath(req.modelPath)
        val validatedMmproj = PathValidator.validateModelPath(req.mmprojPath)
        service.initModel(
            modelPath = validatedModel,
            mmprojPath = validatedMmproj,
        ).fold(
            onSuccess = {
                if (it) ModelRegistry.setMultimodalLoaded(validatedModel, validatedMmproj)
                call.respond(InitModelResponse(ok = it))
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(VLM_ANALYZE_STREAM) {
        val req = call.receive<AnalyzeImageRequest>()
        // Decode before entering the SSE writer — an invalid base64 string must return 400,
        // not leave an empty stream or an uncaught exception after headers are sent.
        val imageBytes = try {
            Base64.getDecoder().decode(req.imageBase64)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, "Invalid base64 image data")
            return@post
        }

        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            val deltas = Channel<String>(capacity = 64)
            val done = Channel<Unit>(capacity = 1)
            val errors = Channel<String>(capacity = 1)
            val cb: GenStream = Sse.genStreamCallback(
                onDelta = { deltas.trySendBlocking(it).isSuccess },
                onDone = { done.trySend(Unit).isSuccess },
                onError = { errors.trySend(it).isSuccess }
            )

            val streamJob = CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO) {
                try {
                    service.analyzeImageBytesStream(imageBytes, req.prompt, cb)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    errors.trySend(e.message ?: "VLM streaming failed")
                }
            }

            try {
                Sse.pipe(writer = this, deltas = deltas, done = done, errors = errors)
            } finally {
                streamJob.cancel()
            }
        }
    }

    post(VLM_CANCEL) {
        service.cancelAnalysis().fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(VLM_RELEASE) {
        service.release().fold(
            onSuccess = {
                ModelRegistry.clearMultimodal()
                call.respond(OkResponse())
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }
}
