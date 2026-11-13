package com.llamatik.host

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Real CIO server on a free port, driven over HTTP: proves the library + engine + approval flow work together. */
class EmbeddedServerTest {

    private class MemoryStore : HostDeviceStore {
        var saved: List<StoredDevice> = emptyList()
        override fun load() = saved
        override fun save(devices: List<StoredDevice>) {
            saved = devices
        }
    }

    private val store = MemoryStore()
    private var busy = false
    private val server = EmbeddedServer(store) { busy }

    @AfterTest
    fun tearDown() = server.stop()

    private fun freePort() = ServerSocket(0).use { it.localPort }

    private fun call(
        port: Int, method: String, path: String, body: String? = null, token: String? = null,
    ): Pair<Int, String> {
        val conn = URL("http://127.0.0.1:$port$path").openConnection() as HttpURLConnection
        conn.requestMethod = method
        token?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        if (body != null) {
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toByteArray()) }
        }
        val code = conn.responseCode
        val text = (if (code < 400) conn.inputStream else conn.errorStream)?.bufferedReader()?.readText().orEmpty()
        return code to text
    }

    private fun startServer(): Int {
        val port = freePort()
        server.start(HostConfig(port = port, advertise = false))
        runBlocking { withTimeout(10_000) { server.state.first { it is EmbeddedServerState.Running } } }
        return port
    }

    @Test
    fun `full flow - unauthenticated, pair, use, revoke`() {
        val port = startServer()

        // The root answers without credentials, inference does not.
        assertEquals(200, call(port, "GET", "/").first)
        val denied = call(port, "GET", "/v1/models")
        assertEquals(401, denied.first)
        assertTrue(denied.second.contains("pairing_required"))

        // A device asks for access; the host user approves it.
        val request = call(port, "POST", "/v1/pairing/request", """{"clientId":"phone-1","clientName":"Pixel"}""")
        assertEquals(202, request.first)
        val requestId = Regex("\"requestId\":\"([^\"]+)\"").find(request.second)!!.groupValues[1]
        val pending = runBlocking { withTimeout(5_000) { server.pendingRequests.first { it.isNotEmpty() } } }
        assertEquals("Pixel", pending.single().clientName)
        server.approve(requestId)

        val token = Regex("\"token\":\"([^\"]+)\"").find(call(port, "GET", "/v1/pairing/$requestId").second)!!.groupValues[1]
        assertEquals(200, call(port, "GET", "/v1/models", token = token).first)
        assertEquals(1, store.saved.size) // approval persisted

        // Remote clients cannot change the host's state.
        assertEquals(403, call(port, "POST", "/v1/generation/init", "{}", token).first)
        assertEquals(403, call(port, "POST", "/v1/generation/shutdown", "{}", token).first)
        // Features that aren't shared answer 404.
        assertEquals(404, call(port, "POST", "/v1/whisper/transcribe", "{}", token).first)

        // While the app itself is generating, remote requests back off.
        busy = true
        assertEquals(503, call(port, "POST", "/v1/generation/stream", """{"prompt":"hi"}""", token).first)
        busy = false

        // Revoking cuts the device off.
        server.revoke("phone-1")
        assertEquals(401, call(port, "GET", "/v1/models", token = token).first)
        assertTrue(server.totalRequests.value > 0)
    }

    @Test
    fun `reports capabilities from the models the app loaded`() {
        val port = startServer()
        val id = Regex("\"requestId\":\"([^\"]+)\"").find(
            call(port, "POST", "/v1/pairing/request", """{"clientId":"d","clientName":"D"}""").second,
        )!!.groupValues[1]
        server.approve(id)
        val token = Regex("\"token\":\"([^\"]+)\"").find(call(port, "GET", "/v1/pairing/$id").second)!!.groupValues[1]

        server.setLoadedModels(generation = null, embedding = null)
        assertTrue(call(port, "GET", "/v1/server/capabilities", token = token).second.contains("\"text\":false"))

        server.setLoadedModels(generation = "/models/llama.gguf", embedding = null)
        assertTrue(call(port, "GET", "/v1/server/capabilities", token = token).second.contains("\"text\":true"))
    }

    @Test
    fun `stop frees the port`() {
        val port = startServer()
        server.stop()
        runBlocking { withTimeout(10_000) { server.state.first { it is EmbeddedServerState.Stopped } } }
        ServerSocket(port).close() // would throw if still bound
    }
}
