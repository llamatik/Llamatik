package com.llamatik.app.feature.sharing

import com.llamatik.app.platform.HostController
import com.llamatik.app.platform.PlatformInfo
import com.llamatik.host.EmbeddedServer
import com.llamatik.host.EmbeddedServerState
import com.llamatik.host.HostConfig
import com.llamatik.host.HostDeviceStore
import com.llamatik.host.LlamatikHost
import com.llamatik.host.LoadedModels
import com.llamatik.host.LocalInferenceActivity
import com.llamatik.host.StoredDevice
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

private const val PORT_KEY = "llamatik_share_port"
private const val DEVICES_KEY = "llamatik_share_devices"

/** A client counts as "connected" if it made an authenticated request within this window. */
private const val CONNECTED_WINDOW_MS = 30_000L
private const val STATUS_TICK_MS = 5_000L

/** Live summary of this device acting as a host, for Settings and the chat top bar. */
data class SharingStatus(
    val isOn: Boolean = false,
    /** Approved devices that talked to this host recently (clients poll it while connected). */
    val connectedClients: Int = 0,
    /** Requests being served for other devices right now. */
    val activeRequests: Int = 0,
) {
    val isGeneratingForOthers: Boolean get() = isOn && activeRequests > 0
}

/** Approved devices persisted as JSON in app settings so approvals survive restarts. */
class SettingsHostDeviceStore(private val settings: Settings) : HostDeviceStore {
    @Serializable
    private data class Record(val clientId: String, val name: String, val token: String, val lastSeen: Long)

    override fun load(): List<StoredDevice> = runCatching {
        Json.decodeFromString<List<Record>>(settings.getString(DEVICES_KEY, "[]"))
            .map { StoredDevice(it.clientId, it.name, it.token, it.lastSeen) }
    }.getOrDefault(emptyList())

    override fun save(devices: List<StoredDevice>) {
        settings.putString(
            DEVICES_KEY,
            Json.encodeToString(devices.map { Record(it.clientId, it.name, it.token, it.lastSeenMillis) }),
        )
    }
}

/**
 * Owns the embedded server and keeps it in sync with the app: it advertises whichever models the
 * main chat has loaded ([LoadedModels]), and reports when the app itself is generating so remote requests back off.
 *
 * Sharing is never started automatically; the user turns it on each time from Settings.
 */
class ShareDeviceRepository(
    private val settings: Settings,
) {
    val server: EmbeddedServer = EmbeddedServer(
        store = SettingsHostDeviceStore(settings),
        isLocalBusy = { LoadedModels.isGenerating.value || LocalInferenceActivity.isBusy },
    ).also { LlamatikHost.server = it }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            combine(LoadedModels.generation, LoadedModels.embedding) { g, e -> g to e }
                .collect { (generation, embedding) -> server.setLoadedModels(generation, embedding) }
        }
    }

    val isSupported: Boolean get() = server.isSupported

    /** Re-evaluated every few seconds because "connected" depends on elapsed time, not just state. */
    private val ticker = flow {
        while (true) {
            emit(Clock.System.now().toEpochMilliseconds())
            delay(STATUS_TICK_MS)
        }
    }

    val status: StateFlow<SharingStatus> = combine(
        server.state, server.devices, server.activeRequests, ticker,
    ) { state, devices, active, now ->
        val on = state is EmbeddedServerState.Running
        SharingStatus(
            isOn = on,
            connectedClients = if (on) devices.count { now - it.lastSeenMillis <= CONNECTED_WINDOW_MS } else 0,
            activeRequests = if (on) active else 0,
        )
    }.stateIn(scope, SharingStarted.Eagerly, SharingStatus())

    var port: Int
        get() = settings.getInt(PORT_KEY, HostConfig.DEFAULT_PORT)
        set(value) = settings.putInt(PORT_KEY, value)

    fun start() {
        HostController.start(server, HostConfig(port = port, serviceName = PlatformInfo.deviceName))
    }

    fun stop() = HostController.stop(server)
}
