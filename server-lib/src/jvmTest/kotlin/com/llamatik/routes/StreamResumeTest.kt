package com.llamatik.routes

import com.llamatik.withTestApp
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreamResumeTest {

    private suspend fun io.ktor.client.HttpClient.stream(streamId: String?, lastEventId: Int? = null) =
        post("/v1/generation/stream") {
            contentType(ContentType.Application.Json)
            streamId?.let { header("X-Stream-Id", it) }
            lastEventId?.let { header("Last-Event-ID", it.toString()) }
            setBody("""{"prompt":"hello"}""")
        }

    @Test
    fun `events carry ids and the response advertises resume support`() = withTestApp {
        val response = client.stream("resume-ids")
        assertEquals("1", response.headers["X-Stream-Resume"])
        assertEquals("resume-ids", response.headers["X-Stream-Id"])
        val body = response.bodyAsText()
        // fake emits "hello", " world" then done
        assertTrue(body.contains("id: 1\ndata: {\"event\":\"delta\",\"text\":\"hello\"}"))
        assertTrue(body.contains("id: 3\ndata: {\"event\":\"done\"}"))
    }

    @Test
    fun `resuming replays only the missed events without regenerating`() = withTestApp {
        client.stream("resume-replay").bodyAsText()

        val resumed = client.stream("resume-replay", lastEventId = 1).bodyAsText()
        assertFalse(resumed.contains("hello"), "already received delta must not be replayed")
        assertTrue(resumed.contains("id: 2\ndata: {\"event\":\"delta\",\"text\":\" world\"}"))
        assertTrue(resumed.contains("\"event\":\"done\""))
    }

    @Test
    fun `resuming a fully received stream just returns done`() = withTestApp {
        client.stream("resume-done").bodyAsText()
        val resumed = client.stream("resume-done", lastEventId = 2).bodyAsText()
        assertEquals("id: 3\ndata: {\"event\":\"done\"}\n\n", resumed)
    }

    @Test
    fun `resuming an unknown stream returns 410`() = withTestApp {
        val response = client.stream("never-existed", lastEventId = 5)
        assertEquals(HttpStatusCode.Gone, response.status)
    }

    @Test
    fun `resuming past the end of the log returns 410`() = withTestApp {
        client.stream("resume-too-far").bodyAsText()
        assertEquals(HttpStatusCode.Gone, client.stream("resume-too-far", lastEventId = 99).status)
    }

    @Test
    fun `re-sending the first request with the same id attaches instead of regenerating`() = withTestApp {
        val first = client.stream("resume-idempotent").bodyAsText()
        val second = client.stream("resume-idempotent").bodyAsText()
        assertEquals(first, second)
    }
}
