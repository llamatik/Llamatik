package com.llamatik

import com.llamatik.routes.embeddingRoutes
import com.llamatik.routes.generationRoutes
import com.llamatik.routes.multimodalRoutes
import com.llamatik.routes.pairingRoutes
import com.llamatik.routes.serverCapabilitiesRoutes
import com.llamatik.routes.stableDiffusionRoutes
import com.llamatik.routes.statusRoutes
import com.llamatik.routes.whisperRoutes
import com.llamatik.util.PathValidator
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.PipelineCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext

private const val BUSY_RETRY_AFTER_SECONDS = "2"
private const val BEARER_PREFIX = "Bearer "

/**
 * Installs the Llamatik inference API (`/v1/...`) on any Ktor engine.
 *
 * - Adds JSON content negotiation and an `IllegalArgumentException → 400` handler unless the host
 *   already installed those plugins.
 * - Enforces [LlamatikServerConfig]: per-device pairing, disabled features (404), admin endpoints
 *   (403), host-busy (503) and request metrics.
 * - [routeWrapper] lets a host wrap the routes (the standalone server wraps them in `authenticate`).
 */
fun Application.installLlamatikServer(
    config: LlamatikServerConfig = LlamatikServerConfig(),
    routeWrapper: Route.(Route.() -> Unit) -> Unit = { it() },
) {
    if (config.modelsDir != null || config.sessionsDir != null) {
        PathValidator.configure(config.modelsDir, config.sessionsDir)
    }
    if (pluginOrNull(ContentNegotiation) == null) {
        install(ContentNegotiation) { json() }
    }
    if (pluginOrNull(StatusPages) == null) {
        install(StatusPages) {
            exception<IllegalArgumentException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request")
            }
        }
    }

    intercept(ApplicationCallPipeline.Plugins) { guard(config) }

    routing {
        get("/") { call.respondText("Welcome to Llamatik Server!") }
        pairingRoutes(config.pairing)
        serverCapabilitiesRoutes(config.features)
        routeWrapper { llamatikInferenceRoutes() }
    }
}

/** The inference routes only, without guards — for hosts that compose their own pipeline. */
fun Route.llamatikInferenceRoutes() {
    statusRoutes()
    embeddingRoutes()
    generationRoutes()
    whisperRoutes()
    stableDiffusionRoutes()
    multimodalRoutes()
}

private val ADMIN_SUFFIXES = listOf(
    "/init", "/release", "/shutdown", "/params", "/session/save", "/session/load", "/session/reset",
)

private fun isAdminPath(path: String) = ADMIN_SUFFIXES.any { path.endsWith(it) } || "/mtp/" in path

private fun featureFor(path: String): ServerFeature? = when {
    path.startsWith("$API_VERSION/generation") -> ServerFeature.TEXT
    path.startsWith("$API_VERSION/embeddings") -> ServerFeature.EMBEDDINGS
    path.startsWith("$API_VERSION/whisper") -> ServerFeature.WHISPER
    path.endsWith("/txt2video") || path.endsWith("/supportsVideo") -> ServerFeature.VIDEO
    path.startsWith("$API_VERSION/stablediffusion") -> ServerFeature.IMAGE
    path.startsWith("$API_VERSION/multimodal") -> ServerFeature.VISION
    else -> null
}

private fun isPairingOrRoot(path: String) = path == "/" || path.startsWith("$API_VERSION/pairing")

@Suppress("ReturnCount")
private suspend fun PipelineContext<Unit, PipelineCall>.guard(config: LlamatikServerConfig) {
    val path = call.request.path()
    val metrics = config.metrics

    if (!isPairingOrRoot(path)) {
        val pairing = config.pairing
        if (pairing != null) {
            val token = call.request.headers[HttpHeaders.Authorization]
                ?.takeIf { it.startsWith(BEARER_PREFIX) }?.removePrefix(BEARER_PREFIX)?.trim()
            if (token.isNullOrEmpty() || pairing.authenticate(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, PairingStatusResponse(status = "pairing_required"))
                return finish()
            }
        }
        if (isAdminPath(path) && !config.allowAdminEndpoints) {
            call.respond(HttpStatusCode.Forbidden, "This endpoint is disabled on this server")
            return finish()
        }
        val feature = featureFor(path)
        if (feature != null && feature !in config.features) {
            call.respond(HttpStatusCode.NotFound, "Not available on this server")
            return finish()
        }
        if (feature != null && call.request.httpMethod == HttpMethod.Post && config.isBusy()) {
            call.response.header(HttpHeaders.RetryAfter, BUSY_RETRY_AFTER_SECONDS)
            call.respond(HttpStatusCode.ServiceUnavailable, "Device is busy")
            return finish()
        }
    }

    metrics.started()
    try {
        proceed()
    } finally {
        metrics.finished()
    }
}
