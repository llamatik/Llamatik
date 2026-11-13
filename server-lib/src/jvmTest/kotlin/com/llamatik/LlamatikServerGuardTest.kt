package com.llamatik

import com.llamatik.status.ModelRegistry
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * These tests only exercise what the guard decides *before* a route handler runs
 * (401/403/404/503, pairing, capabilities), so they don't need the native bridges.
 */
class LlamatikServerGuardTest {

    private fun withServer(config: LlamatikServerConfig, block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            ModelRegistry.reset()
            application { installLlamatikServer(config) }
            block()
        }

    @Test
    fun `root answers without auth even when pairing is on`() =
        withServer(LlamatikServerConfig(pairing = PairingManager())) {
            assertEquals(HttpStatusCode.OK, client.get("/").status)
        }

    @Test
    fun `inference without token gets pairing_required`() =
        withServer(LlamatikServerConfig(pairing = PairingManager())) {
            val r = client.get("/v1/models")
            assertEquals(HttpStatusCode.Unauthorized, r.status)
            assertTrue(r.bodyAsText().contains("pairing_required"))
        }

    @Test
    fun `wrong token is rejected`() =
        withServer(LlamatikServerConfig(pairing = PairingManager())) {
            val r = client.get("/v1/models") { header(HttpHeaders.Authorization, "Bearer nope") }
            assertEquals(HttpStatusCode.Unauthorized, r.status)
        }

    @Test
    fun `approved device can use its token until revoked`() {
        val pairing = PairingManager()
        withServer(LlamatikServerConfig(pairing = pairing)) {
            val req = client.post("/v1/pairing/request") {
                contentType(ContentType.Application.Json)
                setBody("""{"clientId":"dev-1","clientName":"Pixel"}""")
            }
            assertEquals(HttpStatusCode.Accepted, req.status)
            val id = Json.parseToJsonElement(req.bodyAsText()).jsonObject["requestId"]!!.jsonPrimitive.content

            val pending = client.get("/v1/pairing/$id")
            assertEquals(HttpStatusCode.OK, pending.status)
            assertEquals("pending", Json.parseToJsonElement(pending.bodyAsText()).jsonObject["status"]!!.jsonPrimitive.content)

            assertTrue(pairing.approve(id))
            val approved = Json.parseToJsonElement(client.get("/v1/pairing/$id").bodyAsText()).jsonObject
            val token = approved["token"]!!.jsonPrimitive.content
            // The token is handed out once.
            assertNull(Json.parseToJsonElement(client.get("/v1/pairing/$id").bodyAsText()).jsonObject["token"]?.takeIf { it.toString() != "null" })

            val ok = client.get("/v1/models") { header(HttpHeaders.Authorization, "Bearer $token") }
            assertEquals(HttpStatusCode.OK, ok.status)

            pairing.revoke("dev-1")
            val revoked = client.get("/v1/models") { header(HttpHeaders.Authorization, "Bearer $token") }
            assertEquals(HttpStatusCode.Unauthorized, revoked.status)
        }
    }

    @Test
    fun `denied request returns 403`() {
        val pairing = PairingManager()
        withServer(LlamatikServerConfig(pairing = pairing)) {
            val id = pairing.request("dev-2", "Tablet")!!
            assertTrue(pairing.deny(id))
            assertEquals(HttpStatusCode.Forbidden, client.get("/v1/pairing/$id").status)
        }
    }

    @Test
    fun `pending requests are capped`() {
        val pairing = PairingManager(maxPending = 2)
        assertNotNull(pairing.request("a", "A"))
        assertNotNull(pairing.request("b", "B"))
        assertNull(pairing.request("c", "C"))
        // same device asking again reuses its request
        assertEquals(pairing.request("a", "A"), pairing.pending.value.first { it.clientId == "a" }.id)
    }

    @Test
    fun `requests expire`() {
        var now = 0L
        val pairing = PairingManager(requestTtlMillis = 1_000, clock = { now })
        val id = pairing.request("a", "A")!!
        now = 5_000
        assertEquals(PairingManager.Status.EXPIRED, pairing.poll(id).first)
        assertTrue(pairing.pending.value.isEmpty())
    }

    @Test
    fun `admin endpoints are forbidden when disabled`() =
        withServer(LlamatikServerConfig(allowAdminEndpoints = false)) {
            for (path in listOf(
                "/v1/generation/init", "/v1/generation/shutdown", "/v1/generation/params",
                "/v1/generation/session/save", "/v1/generation/session/load", "/v1/generation/session/reset",
                "/v1/generation/mtp/init", "/v1/embeddings/init", "/v1/whisper/release",
            )) {
                val r = client.post(path) { contentType(ContentType.Application.Json); setBody("{}") }
                assertEquals(HttpStatusCode.Forbidden, r.status, path)
            }
        }

    @Test
    fun `disabled features answer 404`() =
        withServer(LlamatikServerConfig(features = setOf(ServerFeature.TEXT))) {
            for (path in listOf("/v1/embeddings/embed", "/v1/whisper/transcribe", "/v1/stablediffusion/txt2img",
                "/v1/stablediffusion/txt2video", "/v1/multimodal/analyzeStream")) {
                val r = client.post(path) { contentType(ContentType.Application.Json); setBody("{}") }
                assertEquals(HttpStatusCode.NotFound, r.status, path)
            }
        }

    @Test
    fun `busy host answers 503 with Retry-After`() =
        withServer(LlamatikServerConfig(isBusy = { true })) {
            val r = client.post("/v1/generation/stream") {
                contentType(ContentType.Application.Json)
                setBody("""{"prompt":"hi"}""")
            }
            assertEquals(HttpStatusCode.ServiceUnavailable, r.status)
            assertNotNull(r.headers[HttpHeaders.RetryAfter])
        }

    @Test
    fun `capabilities reflect enabled and loaded`() =
        withServer(LlamatikServerConfig(features = setOf(ServerFeature.TEXT, ServerFeature.EMBEDDINGS))) {
            fun parse(text: String) = Json.parseToJsonElement(text).jsonObject.mapValues { it.value.jsonPrimitive.content }

            assertEquals("false", parse(client.get("/v1/server/capabilities").bodyAsText())["text"])

            ModelRegistry.setGenerationLoaded("/m/llama.gguf", "instruct", true)
            ModelRegistry.setWhisperLoaded("/m/whisper.bin")

            val caps = parse(client.get("/v1/server/capabilities").bodyAsText())
            assertEquals("true", caps["text"])
            // whisper is loaded but not enabled for this server
            assertEquals("false", caps["audio"])
        }

    @Test
    fun `metrics count requests`() {
        val config = LlamatikServerConfig()
        withServer(config) {
            client.get("/")
            client.get("/")
            assertEquals(2L, config.metrics.totalRequests.value)
            assertEquals(0, config.metrics.activeRequests.value)
        }
    }
}
