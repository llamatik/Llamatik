package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.InitModelResponse
import com.llamatik.api.OkResponse
import com.llamatik.api.TranscribeRequest
import com.llamatik.api.TranscribeResponse
import com.llamatik.api.TranscribeSegmentsRequest
import com.llamatik.api.TranscribeSegmentsResponse
import com.llamatik.api.WhisperInitRequest
import com.llamatik.status.ModelRegistry
import com.llamatik.util.PathValidator
import com.llamatik.util.sanitizedMessage
import com.llamatik.whisper.WhisperService
import com.llamatik.whisper.WhisperServicePort
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

private const val WHISPER = "$API_VERSION/whisper"
private const val WHISPER_INIT = "$WHISPER/init"
private const val WHISPER_TRANSCRIBE = "$WHISPER/transcribe"
private const val WHISPER_TRANSCRIBE_SEGMENTS = "$WHISPER/transcribeSegments"
private const val WHISPER_RELEASE = "$WHISPER/release"

fun Route.whisperRoutes(service: WhisperServicePort = WhisperService) {
    post(WHISPER_INIT) {
        val req = call.receive<WhisperInitRequest>()
        // Throws IllegalArgumentException on invalid path — caught by StatusPages → 400
        val validatedPath = PathValidator.validateModelPath(req.modelPath)
        service.initModel(validatedPath).fold(
            onSuccess = {
                if (it) ModelRegistry.setWhisperLoaded(validatedPath)
                call.respond(InitModelResponse(ok = it))
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(WHISPER_TRANSCRIBE) {
        val req = call.receive<TranscribeRequest>()
        // wavPath also validated; StatusPages handles any IllegalArgumentException
        service.transcribeWav(PathValidator.validateModelPath(req.wavPath), req.language, req.initialPrompt).fold(
            onSuccess = { call.respond(TranscribeResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(WHISPER_TRANSCRIBE_SEGMENTS) {
        val req = call.receive<TranscribeSegmentsRequest>()
        service.transcribeWavSegments(
            PathValidator.validateModelPath(req.wavPath),
            req.language,
            req.initialPrompt,
            req.translate,
            req.diarize,
        ).fold(
            onSuccess = { call.respond(TranscribeSegmentsResponse(json = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(WHISPER_RELEASE) {
        service.release().fold(
            onSuccess = {
                ModelRegistry.clearWhisper()
                call.respond(OkResponse())
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }
}
