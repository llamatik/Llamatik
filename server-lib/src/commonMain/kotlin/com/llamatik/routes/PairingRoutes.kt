package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.PairingManager
import com.llamatik.PairingRequestBody
import com.llamatik.PairingRequestResponse
import com.llamatik.PairingStatusResponse
import com.llamatik.ServerFeature
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

private const val PAIRING = "$API_VERSION/pairing"
private const val MAX_CLIENT_ID_LENGTH = 128

/** `POST /v1/pairing/request` and `GET /v1/pairing/{id}`. No-op when [manager] is null. */
fun Route.pairingRoutes(manager: PairingManager?) {
    if (manager == null) return

    post("$PAIRING/request") {
        val body = call.receive<PairingRequestBody>()
        if (body.clientId.isBlank() || body.clientId.length > MAX_CLIENT_ID_LENGTH) {
            return@post call.respond(HttpStatusCode.BadRequest, "Invalid clientId")
        }
        val id = manager.request(body.clientId, body.clientName.ifBlank { "Unknown device" })
            ?: return@post call.respond(HttpStatusCode.TooManyRequests, "Too many pending requests")
        call.respond(HttpStatusCode.Accepted, PairingRequestResponse(id))
    }

    get("$PAIRING/{id}") {
        val id = call.parameters["id"].orEmpty()
        val (status, token) = manager.poll(id)
        val response = PairingStatusResponse(status.name.lowercase(), token)
        when (status) {
            PairingManager.Status.APPROVED, PairingManager.Status.PENDING -> call.respond(response)
            PairingManager.Status.DENIED -> call.respond(HttpStatusCode.Forbidden, response)
            PairingManager.Status.EXPIRED -> call.respond(HttpStatusCode.NotFound, response)
        }
    }
}

@Serializable
data class ServerCapabilitiesResponse(
    val text: Boolean,
    val image: Boolean,
    val vision: Boolean,
    val video: Boolean,
    val audio: Boolean,
)

/** `GET /v1/server/capabilities` — what this server can do *right now* (enabled AND loaded). */
fun Route.serverCapabilitiesRoutes(features: Set<ServerFeature>) {
    get("$API_VERSION/server/capabilities") {
        val m = com.llamatik.status.ModelRegistry
        call.respond(
            ServerCapabilitiesResponse(
                text = ServerFeature.TEXT in features && m.generation.loaded,
                image = ServerFeature.IMAGE in features && m.stableDiffusion.loaded,
                vision = ServerFeature.VISION in features && m.multimodal.loaded,
                video = ServerFeature.VIDEO in features && m.stableDiffusion.loaded &&
                    m.stableDiffusion.supportsVideo == true,
                audio = ServerFeature.WHISPER in features && m.whisper.loaded,
            )
        )
    }
}
