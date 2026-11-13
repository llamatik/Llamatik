package com.llamatik.routes

import com.llamatik.core.platform.GenStream
import com.llamatik.fakes.FakeMultimodalService
import com.llamatik.multimodal.MultimodalServicePort
import com.llamatik.withTestApp
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
import kotlin.test.assertTrue

class MultimodalRoutesTest {

    private val sampleImageB64: String by lazy {
        Base64.getEncoder().encodeToString(ByteArray(4) { 0xFF.toByte() })
    }

    @Test
    fun `init returns ok true on success`() = withTestApp {
        val response = client.post("/v1/multimodal/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/vlm.gguf","mmprojPath":"/models/mmproj.gguf"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `init returns 400 on failure`() = withTestApp(
        multimodal = FakeMultimodalService(initResult = Result.failure(RuntimeException("vlm error")))
    ) {
        val response = client.post("/v1/multimodal/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/bad","mmprojPath":"/bad"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("vlm error"))
    }

    @Test
    fun `analyzeStream returns 400 on invalid base64 input`() = withTestApp {
        val response = client.post("/v1/multimodal/analyzeStream") {
            contentType(ContentType.Application.Json)
            setBody("""{"imageBase64":"not!!valid==base64","prompt":"what is this?"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid base64 image data"))
    }

    @Test
    fun `analyzeStream returns SSE delta and done events`() = withTestApp {
        val response = client.post("/v1/multimodal/analyzeStream") {
            contentType(ContentType.Application.Json)
            setBody("""{"imageBase64":"$sampleImageB64","prompt":"what is this?"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/event-stream", response.contentType()?.contentType + "/" + response.contentType()?.contentSubtype)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"event\":\"delta\""))
        assertTrue(body.contains("a cat"))
        assertTrue(body.contains("\"event\":\"done\""))
    }

    @Test
    fun `analyzeStream sends error event on service failure`() = withTestApp(
        multimodal = object : MultimodalServicePort by FakeMultimodalService() {
            override fun analyzeImageBytesStream(imageBytes: ByteArray, prompt: String, callback: GenStream) {
                callback.onError("vlm stream error")
            }
        }
    ) {
        val response = client.post("/v1/multimodal/analyzeStream") {
            contentType(ContentType.Application.Json)
            setBody("""{"imageBase64":"$sampleImageB64","prompt":"what?"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"event\":\"error\""))
        assertTrue(response.bodyAsText().contains("vlm stream error"))
    }

    @Test
    fun `cancel returns ok`() = withTestApp {
        val response = client.post("/v1/multimodal/cancel") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `cancel returns 400 on failure`() = withTestApp(
        multimodal = FakeMultimodalService(cancelResult = Result.failure(RuntimeException("cancel error")))
    ) {
        val response = client.post("/v1/multimodal/cancel") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `release returns ok`() = withTestApp {
        val response = client.post("/v1/multimodal/release") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `release returns 400 on failure`() = withTestApp(
        multimodal = FakeMultimodalService(releaseResult = Result.failure(RuntimeException("release error")))
    ) {
        val response = client.post("/v1/multimodal/release") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
