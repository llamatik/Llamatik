package com.llamatik.routes

import com.llamatik.fakes.FakeLlamaService
import com.llamatik.withTestApp
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmbeddingRoutesTest {

    @Test
    fun `init returns ok true on success`() = withTestApp {
        val response = client.post("/v1/embeddings/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/embed.gguf"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `init returns 400 on failure`() = withTestApp(
        llama = FakeLlamaService(initEmbedResult = Result.failure(RuntimeException("load error")))
    ) {
        val response = client.post("/v1/embeddings/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/bad/path"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("load error"))
    }

    @Test
    fun `embed returns float array`() = withTestApp {
        val response = client.post("/v1/embeddings/embed") {
            contentType(ContentType.Application.Json)
            setBody("""{"input":"hello"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val embedding = body["embedding"]?.jsonArray
        assertEquals(3, embedding?.size)
    }

    @Test
    fun `embed returns 400 on failure`() = withTestApp(
        llama = FakeLlamaService(embedResult = Result.failure(RuntimeException("embed error")))
    ) {
        val response = client.post("/v1/embeddings/embed") {
            contentType(ContentType.Application.Json)
            setBody("""{"input":"hello"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
