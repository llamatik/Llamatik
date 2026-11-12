package com.llamatik.host

import com.llamatik.host.LocalInferenceActivity.MAX_BUSY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Lifecycle of the embedded server. */
sealed interface EmbeddedServerState {
    data object Stopped : EmbeddedServerState
    data object Starting : EmbeddedServerState

    /** [addresses] are the `ip` strings other devices can reach this one on. */
    data class Running(val port: Int, val addresses: List<String>) : EmbeddedServerState
    data class Error(val message: String) : EmbeddedServerState
}

data class HostConfig(
    val port: Int = DEFAULT_PORT,
    /** Name shown to other devices (mDNS service name). */
    val serviceName: String = "Llamatik",
    val shareText: Boolean = true,
    val shareEmbeddings: Boolean = true,
    /** Announce the server on the local network (mDNS) so other devices can discover it. */
    val advertise: Boolean = true,
) {
    companion object {
        const val DEFAULT_PORT = 8080
    }
}

/** A device asking to use this one. */
data class PairingRequestInfo(val id: String, val clientId: String, val clientName: String)

/** A device the user approved. */
data class ApprovedDeviceInfo(val clientId: String, val name: String, val lastSeenMillis: Long)

/** Approved-device record including its secret token, for persistence only. */
data class StoredDevice(val clientId: String, val name: String, val token: String, val lastSeenMillis: Long)

/** Persists approved devices so approvals survive restarts. */
interface HostDeviceStore {
    fun load(): List<StoredDevice>
    fun save(devices: List<StoredDevice>)
}

/**
 * Serves this device's already-loaded models to other Llamatik devices.
 *
 * Supported on Android and desktop ([isSupported]); on other platforms [start] reports an error.
 * The HTTP API is the Llamatik-Server library's, restricted to text generation and embeddings,
 * with admin endpoints disabled and per-device approval required.
 */
expect class EmbeddedServer(store: HostDeviceStore, isLocalBusy: () -> Boolean) {
    val isSupported: Boolean
    val state: StateFlow<EmbeddedServerState>
    val pendingRequests: StateFlow<List<PairingRequestInfo>>
    val devices: StateFlow<List<ApprovedDeviceInfo>>
    val activeRequests: StateFlow<Int>
    val totalRequests: StateFlow<Long>

    fun start(config: HostConfig)
    fun stop()

    fun approve(requestId: String)
    fun deny(requestId: String)
    fun revoke(clientId: String)

    /**
     * Tell the server which models the app has loaded (file name or path; null = none), so
     * `/v1/models` and `/v1/server/capabilities` reflect reality. Loading itself stays in the app.
     */
    fun setLoadedModels(generation: String?, embedding: String?)
}

/** Process-wide holder so Android's foreground service and the UI use the same instance. */
object LlamatikHost {
    var server: EmbeddedServer? = null
}

/**
 * What the app currently has loaded and whether it is generating, as published by the app's main
 * chat. The embedded server only *shares* these; loading stays in the app.
 */
object LoadedModels {
    /** File name (or path) of the loaded text-generation model; null when none. */
    val generation = MutableStateFlow<String?>(null)

    /** File name (or path) of the loaded embedding model; null when none. */
    val embedding = MutableStateFlow<String?>(null)

    /** True while the app's own chat is generating. */
    val isGenerating = MutableStateFlow(false)
}

/**
 * Marks the app's own inference as running so remote requests get `503 Retry-After` instead of
 * interleaving with it on the (non re-entrant) bridge.
 *
 * A missed `end()` (e.g. a native callback that never fires) must not block remote clients
 * forever, so a "busy" mark older than [MAX_BUSY] is ignored.
 */
object LocalInferenceActivity {
    private val MAX_BUSY = 10.minutes

    private val running = MutableStateFlow(0)
    private var lastBegin: TimeMark = TimeSource.Monotonic.markNow()

    val isBusy: Boolean get() = running.value > 0 && lastBegin.elapsedNow() < MAX_BUSY

    fun begin() {
        lastBegin = TimeSource.Monotonic.markNow()
        running.update { it + 1 }
    }

    fun end() = running.update { (it - 1).coerceAtLeast(0) }
}
