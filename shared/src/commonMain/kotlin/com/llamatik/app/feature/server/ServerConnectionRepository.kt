package com.llamatik.app.feature.server

import co.touchlab.kermit.Logger
import com.llamatik.app.platform.PlatformInfo
import com.llamatik.network.client.LlamatikServerClient
import com.llamatik.network.client.LlamatikServerConfig
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val ACTIVE_SERVER_HOST_KEY = "llamatik_server_host"
private const val ACTIVE_SERVER_PORT_KEY = "llamatik_server_port"
private const val ACTIVE_SERVER_NAME_KEY = "llamatik_server_name"
private const val SAVED_SERVERS_COUNT_KEY = "llamatik_saved_servers_count"
private const val SAVED_SERVERS_PREFIX = "llamatik_saved_server_"

private const val HEALTH_POLL_INTERVAL_MS = 10_000L
private const val CLIENT_ID_KEY = "llamatik_client_id"
private const val SERVER_TOKEN_PREFIX = "llamatik_server_token_"
private const val PAIRING_POLL_INTERVAL_MS = 1_000L
private const val PAIRING_TIMEOUT_MS = 65_000L

/** Progress of asking a server's owner to approve this device. */
sealed interface PairingProgress {
    data object Idle : PairingProgress

    /** The request was sent; the owner of [serverName] has to approve it on their device. */
    data class Waiting(val serverName: String) : PairingProgress
    data object Denied : PairingProgress
    data object Expired : PairingProgress
}

/** Live health of the active server, shown as a coloured dot in the UI. */
enum class ServerHealth {
    /** Reachable and a text generation model is loaded. */
    ONLINE,

    /** Reachable but not fully usable (e.g. no text model loaded). */
    DEGRADED,

    /** Not reachable. */
    OFFLINE,
}

class ServerConnectionRepository(private val settings: Settings) {

    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _health = MutableStateFlow<ServerHealth?>(null)

    /** Null when no server is connected. */
    val health: StateFlow<ServerHealth?> = _health.asStateFlow()

    private val _pairing = MutableStateFlow<PairingProgress>(PairingProgress.Idle)
    val pairing: StateFlow<PairingProgress> = _pairing.asStateFlow()

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
        startHealthMonitoring()
    }

    /** Polls the active server while one is connected; restarts when the server changes. */
    private fun startHealthMonitoring() {
        monitorScope.launch {
            _activeServer
                .map { server -> server?.let { it.host to it.port } }
                .distinctUntilChanged()
                .collectLatest { target ->
                    if (target == null) {
                        _health.value = null
                        return@collectLatest
                    }
                    val client = LlamatikServerClient(configFor(target.first, target.second))
                    while (true) {
                        val status = client.getModelsStatus()
                        if (status == null) {
                            _health.value = ServerHealth.OFFLINE
                        } else {
                            _health.value =
                                if (status.generation.loaded) ServerHealth.ONLINE else ServerHealth.DEGRADED
                            _activeServer.value = _activeServer.value?.copy(
                                capabilities = ServerCapabilities(
                                    hasText = status.generation.loaded,
                                    hasImage = status.stableDiffusion.loaded,
                                    hasVideo = status.stableDiffusion.supportsVideo ?: false,
                                    hasVision = status.multimodal.loaded,
                                    hasAudio = status.whisper.loaded,
                                )
                            )
                        }
                        delay(HEALTH_POLL_INTERVAL_MS)
                    }
                }
        }
    }

    /** Try to connect to a server: test reachability, fetch capabilities, save as active. */
    suspend fun connectToServer(host: String, port: Int, name: String = host): ConnectedServer? {
        var client = LlamatikServerClient(configFor(host, port))
        val reachable = client.isReachable()
        if (!reachable) {
            Logger.w { "ServerConnectionRepository - $host:$port not reachable" }
            return null
        }
        if (client.probeAccess() == LlamatikServerClient.AccessProbe.PAIRING_REQUIRED) {
            // No token yet, or it was revoked: ask the owner to approve this device.
            settings.remove(tokenKey(host, port))
            if (!pairWith(host, port, name)) return null
            client = LlamatikServerClient(configFor(host, port))
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
        return LlamatikServerClient(configFor(server.host, server.port))
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
        val client = LlamatikServerClient(configFor(current.host, current.port))
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

    private fun configFor(host: String, port: Int) =
        LlamatikServerConfig(host, port, accessToken = settings.getStringOrNull(tokenKey(host, port)))

    private fun tokenKey(host: String, port: Int) = "$SERVER_TOKEN_PREFIX${host}_$port"

    /** Stable random id for this installation, so a server can recognise the device it approved. */
    private fun clientId(): String =
        settings.getStringOrNull(CLIENT_ID_KEY) ?: buildString {
            repeat(4) { append(Random.nextLong().toULong().toString(16).padStart(16, '0')) }
        }.also { settings.putString(CLIENT_ID_KEY, it) }

    /**
     * Asks the server's owner to approve this device and waits for the answer. On approval the
     * issued token is stored for this server. Progress is exposed through [pairing].
     */
    private suspend fun pairWith(host: String, port: Int, name: String): Boolean {
        val client = LlamatikServerClient(LlamatikServerConfig(host, port))
        val requestId = client.requestPairing(clientId(), PlatformInfo.deviceName)
        if (requestId == null) {
            _pairing.value = PairingProgress.Denied
            return false
        }
        _pairing.value = PairingProgress.Waiting(name)
        try {
            var waited = 0L
            while (waited < PAIRING_TIMEOUT_MS) {
                val poll = client.pollPairing(requestId)
                when (poll?.status) {
                    "approved" -> {
                        val token = poll.token ?: return false
                        settings.putString(tokenKey(host, port), token)
                        _pairing.value = PairingProgress.Idle
                        return true
                    }
                    "denied" -> {
                        _pairing.value = PairingProgress.Denied
                        return false
                    }
                    "expired" -> {
                        _pairing.value = PairingProgress.Expired
                        return false
                    }
                }
                delay(PAIRING_POLL_INTERVAL_MS)
                waited += PAIRING_POLL_INTERVAL_MS
            }
            _pairing.value = PairingProgress.Expired
            return false
        } finally {
            if (_pairing.value is PairingProgress.Waiting) _pairing.value = PairingProgress.Idle
        }
    }

    fun clearPairingResult() {
        _pairing.value = PairingProgress.Idle
    }

    private fun persistActiveServer(server: ConnectedServer) {
        settings.putString(ACTIVE_SERVER_HOST_KEY, server.host)
        settings.putInt(ACTIVE_SERVER_PORT_KEY, server.port)
        settings.putString(ACTIVE_SERVER_NAME_KEY, server.name)
    }
}
