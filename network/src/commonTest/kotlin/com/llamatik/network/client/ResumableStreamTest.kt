package com.llamatik.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResumableStreamTest {

    // Real dispatcher: the idle-timeout inside the client must not be fast-forwarded by virtual time
    // while the mock engine delivers bytes on another thread.
    private fun realTimeTest(block: suspend () -> Unit) = runTest { withContext(Dispatchers.Default) { block() } }

    private fun delta(id: Int, text: String) = "id: $id\ndata: {\"event\":\"delta\",\"text\":\"$text\"}\n\n"
    private fun done(id: Int) = "id: $id\ndata: {\"event\":\"done\"}\n\n"

    private val sseHeaders = headersOf(
        HttpHeaders.ContentType to listOf("text/event-stream"),
        "X-Stream-Resume" to listOf("1"),
    )

    private fun client(engine: MockEngine, maxAttempts: Int = 4) = LlamatikServerClient(
        config = LlamatikServerConfig("localhost", 8080),
        httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json() }
            defaultRequest { contentType(ContentType.Application.Json) }
        },
        retryPolicy = StreamRetryPolicy(maxAttempts = maxAttempts, baseDelayMs = 1, maxDelayMs = 1),
    )

    @Test
    fun `resumes after a dropped connection without duplicating text`() = realTimeTest {
        val lastEventIds = mutableListOf<String?>()
        val streamIds = mutableSetOf<String?>()
        var call = 0
        val engine = MockEngine { request ->
            lastEventIds += request.headers["Last-Event-ID"]
            streamIds += request.headers["X-Stream-Id"]
            if (call++ == 0) {
                // Connection closes after two deltas, before "done".
                respond(delta(1, "Hel") + delta(2, "lo"), HttpStatusCode.OK, sseHeaders)
            } else {
                respond(delta(3, " world") + done(4), HttpStatusCode.OK, sseHeaders)
            }
        }

        val text = client(engine).streamGenerate("hi").toList().joinToString("")

        assertEquals("Hello world", text)
        assertEquals(listOf(null, "2"), lastEventIds)
        assertEquals(1, streamIds.size, "all attempts must reuse the same stream id")
    }

    @Test
    fun `gives up after the configured number of failed reconnects`() = realTimeTest {
        var calls = 0
        val engine = MockEngine {
            calls++
            respond("", HttpStatusCode.OK, sseHeaders) // immediately closed, never "done"
        }
        assertFailsWith<StreamInterruptedException> { client(engine, maxAttempts = 2).streamGenerate("hi").toList() }
        assertEquals(3, calls) // first try + 2 retries
    }

    @Test
    fun `failure counter resets when a retry makes progress`() = realTimeTest {
        var call = 0
        val engine = MockEngine {
            when (call++) {
                0 -> respond(delta(1, "a"), HttpStatusCode.OK, sseHeaders)
                1 -> respond(delta(2, "b"), HttpStatusCode.OK, sseHeaders)
                2 -> respond(delta(3, "c"), HttpStatusCode.OK, sseHeaders)
                else -> respond(done(4), HttpStatusCode.OK, sseHeaders)
            }
        }
        // maxAttempts = 1 would fail if progress did not reset the counter
        assertEquals("abc", client(engine, maxAttempts = 1).streamGenerate("hi").toList().joinToString(""))
    }

    @Test
    fun `410 on resume surfaces as StreamExpiredException`() = realTimeTest {
        var call = 0
        val engine = MockEngine {
            if (call++ == 0) respond(delta(1, "a"), HttpStatusCode.OK, sseHeaders)
            else respondError(HttpStatusCode.Gone)
        }
        assertFailsWith<StreamExpiredException> { client(engine).streamGenerate("hi").toList() }
    }

    @Test
    fun `server without resume support is treated as expired instead of duplicating output`() = realTimeTest {
        var call = 0
        val noResume = headersOf(HttpHeaders.ContentType to listOf("text/event-stream"))
        val engine = MockEngine {
            if (call++ == 0) respond(delta(1, "a"), HttpStatusCode.OK, sseHeaders)
            else respond(delta(1, "a") + done(2), HttpStatusCode.OK, noResume)
        }
        assertFailsWith<StreamExpiredException> { client(engine).streamGenerate("hi").toList() }
    }

    @Test
    fun `server reported errors are not retried`() = realTimeTest {
        var calls = 0
        val engine = MockEngine {
            calls++
            respond("data: {\"event\":\"error\",\"message\":\"boom\"}\n\n", HttpStatusCode.OK, sseHeaders)
        }
        val e = assertFailsWith<RemoteInferenceException> { client(engine).streamGenerate("hi").toList() }
        assertEquals("boom", e.message)
        assertEquals(1, calls)
    }

    @Test
    fun `http client errors are not retried`() = realTimeTest {
        var calls = 0
        val engine = MockEngine { calls++; respondError(HttpStatusCode.BadRequest) }
        assertFailsWith<RemoteInferenceException> { client(engine).streamGenerate("hi").toList() }
        assertEquals(1, calls)
    }
}
