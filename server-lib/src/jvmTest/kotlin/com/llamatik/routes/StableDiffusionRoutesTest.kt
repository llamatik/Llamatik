package com.llamatik.routes

import com.llamatik.fakes.FakeStableDiffusionService
import com.llamatik.withTestApp
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StableDiffusionRoutesTest {

    // ── init ────────────────────────────────────────────────────────────────

    @Test
    fun `init returns ok true on success`() = withTestApp {
        val response = client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/sd.gguf"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `init with threads returns ok`() = withTestApp {
        val response = client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/sd.gguf","threads":4}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `init with video model paths returns ok`() = withTestApp {
        val response = client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"modelPath":"/models/wan2.1.gguf","threads":-1,""" +
                    """"vaePath":"/models/wan_vae.safetensors",""" +
                    """"t5xxlPath":"/models/umt5-xxl.gguf",""" +
                    """"clipVisionPath":null}"""
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `init returns 400 on failure`() = withTestApp(
        sd = FakeStableDiffusionService(initResult = Result.failure(RuntimeException("bad model")))
    ) {
        val response = client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/bad"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ── txt2img ─────────────────────────────────────────────────────────────

    @Test
    fun `txt2img returns base64 image`() = withTestApp {
        val response = client.post("/v1/stablediffusion/txt2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"a sunset"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val b64 = body["imageBase64"]?.jsonPrimitive?.content
        assertFalse(b64.isNullOrEmpty())
        val decoded = Base64.getDecoder().decode(b64)
        assertEquals(512 * 512 * 4, decoded.size)
    }

    @Test
    fun `txt2img with all params returns ok`() = withTestApp {
        val response = client.post("/v1/stablediffusion/txt2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"cat","negativePrompt":"blurry","width":256,"height":256,"steps":10,"cfgScale":5.0,"seed":42}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `txt2img returns 400 on failure`() = withTestApp(
        sd = FakeStableDiffusionService(txt2imgResult = Result.failure(RuntimeException("gen error")))
    ) {
        val response = client.post("/v1/stablediffusion/txt2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `txt2img returns 500 when bytes are empty`() = withTestApp(
        sd = FakeStableDiffusionService(txt2imgResult = Result.success(ByteArray(0)))
    ) {
        val response = client.post("/v1/stablediffusion/txt2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    // ── img2img ─────────────────────────────────────────────────────────────

    @Test
    fun `img2img returns 400 on invalid base64 input`() = withTestApp {
        val response = client.post("/v1/stablediffusion/img2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"initImageBase64":"not!!valid==base64","initImageW":1,"initImageH":1,"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid base64 image data"))
    }

    @Test
    fun `img2img returns base64 image`() = withTestApp {
        val initImageBytes = ByteArray(64 * 64 * 4) { 0x80.toByte() }
        val initImageB64 = Base64.getEncoder().encodeToString(initImageBytes)
        val response = client.post("/v1/stablediffusion/img2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"initImageBase64":"$initImageB64","initImageW":64,"initImageH":64,"prompt":"a cat"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertFalse(body["imageBase64"]?.jsonPrimitive?.content.isNullOrEmpty())
    }

    @Test
    fun `img2img returns 400 on failure`() = withTestApp(
        sd = FakeStableDiffusionService(img2imgResult = Result.failure(RuntimeException("img error")))
    ) {
        val initImageBytes = ByteArray(4) { 0 }
        val b64 = Base64.getEncoder().encodeToString(initImageBytes)
        val response = client.post("/v1/stablediffusion/img2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"initImageBase64":"$b64","initImageW":1,"initImageH":1,"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `img2img returns 500 when bytes are empty`() = withTestApp(
        sd = FakeStableDiffusionService(img2imgResult = Result.success(ByteArray(0)))
    ) {
        val initImageBytes = ByteArray(4) { 0 }
        val b64 = Base64.getEncoder().encodeToString(initImageBytes)
        val response = client.post("/v1/stablediffusion/img2img") {
            contentType(ContentType.Application.Json)
            setBody("""{"initImageBase64":"$b64","initImageW":1,"initImageH":1,"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    // ── release ─────────────────────────────────────────────────────────────

    @Test
    fun `release returns ok`() = withTestApp {
        val response = client.post("/v1/stablediffusion/release") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `release returns 400 on failure`() = withTestApp(
        sd = FakeStableDiffusionService(releaseResult = Result.failure(RuntimeException("release error")))
    ) {
        val response = client.post("/v1/stablediffusion/release") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ── path validation ──────────────────────────────────────────────────────

    @Test
    fun `init returns 400 on path with null byte`() = withTestApp {
        val response = client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            // JSON \u0000 is a valid JSON escape for the null character
            setBody("{\"modelPath\":\"/models/\u0000evil.gguf\"}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ── supportsVideo ────────────────────────────────────────────────────────

    @Test
    fun `supportsVideo returns true when model supports video`() = withTestApp {
        val response = client.get("/v1/stablediffusion/supportsVideo")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["supportsVideo"]?.jsonPrimitive?.content)
    }

    @Test
    fun `supportsVideo returns false when model does not support video`() = withTestApp(
        sd = FakeStableDiffusionService(supportsVideoResult = Result.success(false))
    ) {
        val response = client.get("/v1/stablediffusion/supportsVideo")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("false", body["supportsVideo"]?.jsonPrimitive?.content)
    }

    @Test
    fun `supportsVideo returns 500 on failure`() = withTestApp(
        sd = FakeStableDiffusionService(supportsVideoResult = Result.failure(RuntimeException("no model loaded")))
    ) {
        val response = client.get("/v1/stablediffusion/supportsVideo")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    // ── txt2video ────────────────────────────────────────────────────────────

    @Test
    fun `txt2video returns base64 packed video`() = withTestApp {
        val response = client.post("/v1/stablediffusion/txt2video") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"a cat walking"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val b64 = body["videoBase64"]?.jsonPrimitive?.content
        assertFalse(b64.isNullOrEmpty())
        // Packed header is 16 bytes + 4 frames × 8×8×4 = 16 + 1024 = 1040 bytes
        val decoded = Base64.getDecoder().decode(b64)
        assertTrue(decoded.size >= 16, "Packed video must contain at least the 16-byte header")
    }

    @Test
    fun `txt2video with all params returns ok`() = withTestApp {
        val response = client.post("/v1/stablediffusion/txt2video") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"a dog","negativePrompt":"blurry","width":256,"height":256,"videoFrames":8,"fps":4,"cfgScale":5.0,"seed":42}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `txt2video returns 400 on failure`() = withTestApp(
        sd = FakeStableDiffusionService(txt2videoResult = Result.failure(RuntimeException("video error")))
    ) {
        val response = client.post("/v1/stablediffusion/txt2video") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `txt2video returns 500 when bytes are empty`() = withTestApp(
        sd = FakeStableDiffusionService(txt2videoResult = Result.success(ByteArray(0)))
    ) {
        val response = client.post("/v1/stablediffusion/txt2video") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
