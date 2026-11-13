package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.EmbedRequest
import com.llamatik.api.EmbedResponse
import com.llamatik.api.InitModelRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.llama.LlamaService
import com.llamatik.llama.LlamaServicePort
import com.llamatik.status.ModelRegistry
import com.llamatik.util.PathValidator
import com.llamatik.util.sanitizedMessage
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

const val EMBEDDINGS = "$API_VERSION/embeddings"
const val EMBEDDINGS_INIT = "$EMBEDDINGS/init"
const val EMBEDDINGS_EMBED = "$EMBEDDINGS/embed"

fun Route.embeddingRoutes(service: LlamaServicePort = LlamaService) {
    post(EMBEDDINGS_INIT) {
        val req = call.receive<InitModelRequest>()
        // Throws IllegalArgumentException on invalid path — caught by StatusPages → 400
        val validatedPath = PathValidator.validateModelPath(req.modelPath)
        service.initModel(validatedPath).fold(
            onSuccess = {
                if (it) ModelRegistry.setEmbeddingLoaded(validatedPath)
                call.respond(InitModelResponse(ok = it))
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(EMBEDDINGS_EMBED) {
        val req = call.receive<EmbedRequest>()
        service.embed(req.input).fold(
            onSuccess = { call.respond(EmbedResponse(embedding = it.toList())) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }
}
