package com.llamatik.host

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Hosting needs the JVM-based Llamatik-Server library, so it is unavailable here. Devices on this
 * platform can still *connect* to a Llamatik server (see :network).
 */
actual class EmbeddedServer actual constructor(
    store: HostDeviceStore,
    isLocalBusy: () -> Boolean,
) {
    actual val isSupported: Boolean = false
    actual val state: StateFlow<EmbeddedServerState> =
        MutableStateFlow(EmbeddedServerState.Error("Sharing this device is not supported on this platform"))
    actual val pendingRequests: StateFlow<List<PairingRequestInfo>> = MutableStateFlow(emptyList())
    actual val devices: StateFlow<List<ApprovedDeviceInfo>> = MutableStateFlow(emptyList())
    actual val activeRequests: StateFlow<Int> = MutableStateFlow(0)
    actual val totalRequests: StateFlow<Long> = MutableStateFlow(0L)

    actual fun start(config: HostConfig) = Unit
    actual fun stop() = Unit
    actual fun approve(requestId: String) = Unit
    actual fun deny(requestId: String) = Unit
    actual fun revoke(clientId: String) = Unit
    actual fun setLoadedModels(generation: String?, embedding: String?) = Unit
}
