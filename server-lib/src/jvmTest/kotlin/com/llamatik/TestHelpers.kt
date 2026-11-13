package com.llamatik

import com.llamatik.fakes.FakeLlamaService
import com.llamatik.fakes.FakeMultimodalService
import com.llamatik.fakes.FakeStableDiffusionService
import com.llamatik.fakes.FakeWhisperService
import com.llamatik.llama.LlamaServicePort
import com.llamatik.multimodal.MultimodalServicePort
import com.llamatik.routes.embeddingRoutes
import com.llamatik.routes.generationRoutes
import com.llamatik.routes.multimodalRoutes
import com.llamatik.routes.stableDiffusionRoutes
import com.llamatik.routes.statusRoutes
import com.llamatik.routes.whisperRoutes
import com.llamatik.stablediffusion.StableDiffusionServicePort
import com.llamatik.status.ModelRegistry
import com.llamatik.whisper.WhisperServicePort
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

fun withTestApp(
    llama: LlamaServicePort = FakeLlamaService(),
    whisper: WhisperServicePort = FakeWhisperService(),
    sd: StableDiffusionServicePort = FakeStableDiffusionService(),
    multimodal: MultimodalServicePort = FakeMultimodalService(),
    block: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    // Always start each test with a clean registry so state doesn't leak between tests.
    ModelRegistry.reset()
    application {
        install(ContentNegotiation) { json() }
        // Mirror the StatusPages config from GeneralRoutingConfig so that
        // IllegalArgumentException thrown by PathValidator returns 400 in tests.
        install(StatusPages) {
            exception<IllegalArgumentException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request")
            }
        }
        routing {
            statusRoutes()
            embeddingRoutes(llama)
            generationRoutes(llama)
            whisperRoutes(whisper)
            stableDiffusionRoutes(sd)
            multimodalRoutes(multimodal)
        }
    }
    block()
}
