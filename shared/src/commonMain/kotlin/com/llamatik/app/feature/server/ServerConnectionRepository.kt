package com.llamatik.app.feature.server

import co.touchlab.kermit.Logger
import com.llamatik.network.client.LlamatikServerClient
import com.llamatik.network.client.LlamatikServerConfig
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ACTIVE_SERVER_HOST_KEY = "llamatik_server_host"
private const val ACTIVE_SERVER_PORT_KEY = "llamatik_server_port"
private const val ACTIVE_SERVER_NAME_KEY = "llamatik_server_name"
private const val SAVED_SERVERS_COUNT_KEY = "llamatik_saved_servers_count"
private const val SAVED_SERVERS_PREFIX = "llamatik_saved_server_"

class ServerConnectionRepository(private val settings: Settings) {

    private val _activeServer = MutableStateFlow<ConnectedServer?>(null)
    val activeServer: StateFlow<ConnectedServer?> = _activeServer.asStateFlow()

    init {
        // Restore persisted active server on init (without capabilities — user must reconnect)
        val host = settings.getString(ACTIVE_SERVER_HOST_KEY, "").takeIf { it.isNotBlank() }
        val port = settings.getInt(ACTIVE_SERVER_PORT_KEY, -1).takeIf { it != -1 }
        val name = settings.getString(ACTIVE_SERVER_NAME_KEY, "")
        if (host != null && port != null) {
            _activeServer.value = ConnectedServer(
                host = host,
                port = port,
                name = name.ifBlank { host },
                capabilities = ServerCapabilities() // unknown until reconnected
            )
        }
    }

    /** Try to connect to a server: test reachability, fetch capabilities, save as active. */
    suspend fun connectToServer(host: String, port: Int, name: String = host): ConnectedServer? {
        val config = LlamatikServerConfig(host, port)
        val client = LlamatikServerClient(config)
        val reachable = client.isReachable()
        if (!reachable) {
            Logger.w { "ServerConnectionRepository - $host:$port not reachable" }
            return null
        }
        val status = client.getModelsStatus()
        val capabilities = ServerCapabilities(
            hasText  = status?.generation?.loaded ?: false,
            hasImage = status?.stableDiffusion?.loaded ?: false,
            hasVideo = status?.stableDiffusion?.supportsVideo ?: false,
            hasVision = status?.multimodal?.loaded ?: false,
            hasAudio = status?.whisper?.loaded ?: false,
        )
        val server = ConnectedServer(host = host, port = port, name = name, capabilities = capabilities)
        _activeServer.value = server
        persistActiveServer(server)
        return server
    }

    fun disconnectFromServer() {
        _activeServer.value = null
        settings.putString(ACTIVE_SERVER_HOST_KEY, "")
        settings.putInt(ACTIVE_SERVER_PORT_KEY, -1)
        settings.putString(ACTIVE_SERVER_NAME_KEY, "")
    }

    fun getSavedServers(): List<SavedServer> {
        val count = settings.getInt(SAVED_SERVERS_COUNT_KEY, 0)
        return (0 until count).mapNotNull { i ->
            val host = settings.getString("${SAVED_SERVERS_PREFIX}${i}_host", "").takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val port = settings.getInt("${SAVED_SERVERS_PREFIX}${i}_port", 8080)
            val name = settings.getString("${SAVED_SERVERS_PREFIX}${i}_name", host)
            SavedServer(host, port, name.ifBlank { host })
        }
    }

    fun addSavedServer(host: String, port: Int, name: String = host) {
        val existing = getSavedServers()
        if (existing.any { it.host == host && it.port == port }) return
        val idx = existing.size
        settings.putString("${SAVED_SERVERS_PREFIX}${idx}_host", host)
        settings.putInt("${SAVED_SERVERS_PREFIX}${idx}_port", port)
        settings.putString("${SAVED_SERVERS_PREFIX}${idx}_name", name)
        settings.putInt(SAVED_SERVERS_COUNT_KEY, idx + 1)
    }

    fun removeSavedServer(host: String, port: Int) {
        val updated = getSavedServers().filter { !(it.host == host && it.port == port) }
        settings.putInt(SAVED_SERVERS_COUNT_KEY, updated.size)
        updated.forEachIndexed { i, s ->
            settings.putString("${SAVED_SERVERS_PREFIX}${i}_host", s.host)
            settings.putInt("${SAVED_SERVERS_PREFIX}${i}_port", s.port)
            settings.putString("${SAVED_SERVERS_PREFIX}${i}_name", s.name)
        }
    }

    fun makeClientForActiveServer(): LlamatikServerClient? {
        val server = _activeServer.value ?: return null
        return LlamatikServerClient(LlamatikServerConfig(server.host, server.port))
    }

    /**
     * Re-queries the active server for its current loaded models and updates the
     * stored [ConnectedServer.capabilities]. Call this whenever you need fresh
     * capability data without fully reconnecting (e.g. the server loaded a new
     * model after the initial connection was established).
     *
     * Returns the updated [ConnectedServer], or null if there is no active server.
     */
    suspend fun refreshCapabilities(): ConnectedServer? {
        val current = _activeServer.value ?: return null
        val client = LlamatikServerClient(LlamatikServerConfig(current.host, current.port))
        val status = client.getModelsStatus() ?: return current // keep existing caps on error
        val capabilities = ServerCapabilities(
            hasText   = status.generation.loaded,
            hasImage  = status.stableDiffusion.loaded,
            hasVideo  = status.stableDiffusion.supportsVideo ?: false,
            hasVision = status.multimodal.loaded,
            hasAudio  = status.whisper.loaded,
        )
        val updated = current.copy(capabilities = capabilities)
        _activeServer.value = updated
        return updated
    }

    private fun persistActiveServer(server: ConnectedServer) {
        settings.putString(ACTIVE_SERVER_HOST_KEY, server.host)
        settings.putInt(ACTIVE_SERVER_PORT_KEY, server.port)
        settings.putString(ACTIVE_SERVER_NAME_KEY, server.name)
    }
}
