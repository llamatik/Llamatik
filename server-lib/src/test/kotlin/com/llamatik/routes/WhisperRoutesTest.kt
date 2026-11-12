package com.llamatik.routes

import com.llamatik.fakes.FakeWhisperService
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WhisperRoutesTest {

    @Test
    fun `init returns ok true on success`() = withTestApp {
        val response = client.post("/v1/whisper/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/whisper.bin"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `init returns 400 on failure`() = withTestApp(
        whisper = FakeWhisperService(initResult = Result.failure(RuntimeException("no model")))
    ) {
        val response = client.post("/v1/whisper/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/bad"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("no model"))
    }

    @Test
    fun `transcribe returns text`() = withTestApp {
        val response = client.post("/v1/whisper/transcribe") {
            contentType(ContentType.Application.Json)
            setBody("""{"wavPath":"/audio/test.wav"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("hello world", body["text"]?.jsonPrimitive?.content)
    }

    @Test
    fun `transcribe with optional fields returns text`() = withTestApp {
        val response = client.post("/v1/whisper/transcribe") {
            contentType(ContentType.Application.Json)
            setBody("""{"wavPath":"/audio/test.wav","language":"en","initialPrompt":"hello"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `transcribe returns 400 on failure`() = withTestApp(
        whisper = FakeWhisperService(transcribeResult = Result.failure(RuntimeException("decode error")))
    ) {
        val response = client.post("/v1/whisper/transcribe") {
            contentType(ContentType.Application.Json)
            setBody("""{"wavPath":"/audio/test.wav"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("decode error"))
    }

    @Test
    fun `release returns ok`() = withTestApp {
        val response = client.post("/v1/whisper/release") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `release returns 400 on failure`() = withTestApp(
        whisper = FakeWhisperService(releaseResult = Result.failure(RuntimeException("release error")))
    ) {
        val response = client.post("/v1/whisper/release") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `transcribeSegments returns json`() = withTestApp {
        val response = client.post("/v1/whisper/transcribeSegments") {
            contentType(ContentType.Application.Json)
            setBody("""{"wavPath":"/audio/test.wav"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["json"]?.jsonPrimitive?.content?.contains("segments") == true)
    }

    @Test
    fun `transcribeSegments with all options returns json`() = withTestApp {
        val response = client.post("/v1/whisper/transcribeSegments") {
            contentType(ContentType.Application.Json)
            setBody("""{"wavPath":"/audio/test.wav","language":"en","translate":true,"diarize":false}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `transcribeSegments returns 400 on failure`() = withTestApp(
        whisper = FakeWhisperService(transcribeSegmentsResult = Result.failure(RuntimeException("segments error")))
    ) {
        val response = client.post("/v1/whisper/transcribeSegments") {
            contentType(ContentType.Application.Json)
            setBody("""{"wavPath":"/audio/test.wav"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("segments error"))
    }
}
