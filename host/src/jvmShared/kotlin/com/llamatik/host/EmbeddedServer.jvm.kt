package com.llamatik.host

import com.llamatik.ApprovedDevice
import com.llamatik.DeviceStore
import com.llamatik.LlamatikServerConfig
import com.llamatik.PairingManager
import com.llamatik.ServerFeature
import com.llamatik.installLlamatikServer
import com.llamatik.network.discovery.NodeDiscovery
import com.llamatik.status.ModelRegistry
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

/** Android + desktop implementation: Ktor CIO serving the Llamatik-Server library. */
actual class EmbeddedServer actual constructor(
    store: HostDeviceStore,
    private val isLocalBusy: () -> Boolean,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private val pairing = PairingManager(
        store = object : DeviceStore {
            override fun load() = store.load().map { ApprovedDevice(it.clientId, it.name, it.token, it.lastSeenMillis) }
            override fun save(devices: List<ApprovedDevice>) =
                store.save(devices.map { StoredDevice(it.clientId, it.name, it.token, it.lastSeenMillis) })
        },
    )
    private val serverConfig = LlamatikServerConfigHolder()

    private val _state = MutableStateFlow<EmbeddedServerState>(EmbeddedServerState.Stopped)
    private var engine: io.ktor.server.engine.EmbeddedServer<*, *>? = null
    private var advertised = false

    actual val isSupported: Boolean = true
    actual val state: StateFlow<EmbeddedServerState> get() = _state
    actual val pendingRequests: StateFlow<List<PairingRequestInfo>> = pairing.pending
        .map { list -> list.map { PairingRequestInfo(it.id, it.clientId, it.clientName) } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    actual val devices: StateFlow<List<ApprovedDeviceInfo>> = pairing.devices
        .map { list -> list.map { ApprovedDeviceInfo(it.clientId, it.name, it.lastSeenMillis) } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    actual val activeRequests: StateFlow<Int> get() = serverConfig.metrics.activeRequests
    actual val totalRequests: StateFlow<Long> get() = serverConfig.metrics.totalRequests

    actual fun start(config: HostConfig) {
        synchronized(lock) {
            if (_state.value is EmbeddedServerState.Starting || _state.value is EmbeddedServerState.Running) return
            _state.value = EmbeddedServerState.Starting
        }
        scope.launch {
            @Suppress("TooGenericExceptionCaught")
            try {
                val features = buildSet {
                    if (config.shareText) add(ServerFeature.TEXT)
                    if (config.shareEmbeddings) add(ServerFeature.EMBEDDINGS)
                }
                val libConfig = LlamatikServerConfig(
                    features = features,
                    // Remote devices use what the user loaded; they can't load/unload/reconfigure it.
                    allowAdminEndpoints = false,
                    isBusy = isLocalBusy,
                    pairing = pairing,
                    metrics = serverConfig.metrics,
                )
                val server = embeddedServer(CIO, port = config.port, host = "0.0.0.0") {
                    installLlamatikServer(libConfig)
                }
                server.start(wait = false)
                synchronized(lock) { engine = server }

                if (config.advertise) {
                    runCatching { NodeDiscovery.startAdvertising(config.port, config.serviceName) }
                        .onSuccess { advertised = true }
                }

                _state.value = EmbeddedServerState.Running(config.port, localIpAddresses())
            } catch (e: Exception) {
                _state.value = EmbeddedServerState.Error(e.message ?: "Could not start the server")
            }
        }
    }

    actual fun stop() {
        val running = synchronized(lock) { engine.also { engine = null } }
        if (advertised) {
            runCatching { NodeDiscovery.stopAdvertising() }
            advertised = false
        }
        if (running == null) {
            _state.value = EmbeddedServerState.Stopped
            return
        }
        scope.launch {
            runCatching { running.stop(gracePeriodMillis = 500, timeoutMillis = 2_000) }
            _state.value = EmbeddedServerState.Stopped
        }
    }

    actual fun approve(requestId: String) {
        pairing.approve(requestId)
    }

    actual fun deny(requestId: String) {
        pairing.deny(requestId)
    }

    actual fun revoke(clientId: String) {
        pairing.revoke(clientId)
    }

    actual fun setLoadedModels(generation: String?, embedding: String?) {
        if (generation != null) ModelRegistry.setGenerationLoaded(generation) else ModelRegistry.clearGeneration()
        if (embedding != null) ModelRegistry.setEmbeddingLoaded(embedding)
    }
}

/** Keeps the metrics object alive across start/stop so UI collectors never go stale. */
private class LlamatikServerConfigHolder {
    val metrics = com.llamatik.ServerMetrics()
}

private fun localIpAddresses(): List<String> = runCatching {
    NetworkInterface.getNetworkInterfaces().toList()
        .filter { it.isUp && !it.isLoopback && !it.isVirtual }
        .flatMap { it.inetAddresses.toList() }
        .filterIsInstance<Inet4Address>()
        .filter { !it.isLoopbackAddress }
        .mapNotNull { it.hostAddress }
}.getOrDefault(emptyList())
