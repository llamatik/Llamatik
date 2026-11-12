package com.llamatik.app.feature.sharing

import com.llamatik.app.platform.HostController
import com.llamatik.app.platform.PlatformInfo
import com.llamatik.host.EmbeddedServer
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val PORT_KEY = "llamatik_share_port"
private const val DEVICES_KEY = "llamatik_share_devices"

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

    var port: Int
        get() = settings.getInt(PORT_KEY, HostConfig.DEFAULT_PORT)
        set(value) = settings.putInt(PORT_KEY, value)

    fun start() {
        HostController.start(server, HostConfig(port = port, serviceName = PlatformInfo.deviceName))
    }

    fun stop() = HostController.stop(server)
}
