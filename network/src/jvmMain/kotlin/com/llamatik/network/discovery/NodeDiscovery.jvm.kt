package com.llamatik.network.discovery

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual object NodeDiscovery {
    private const val SERVICE_TYPE = "_llamatik._tcp.local."
    private var jmdns: List<javax.jmdns.JmDNS> = emptyList()
    private var registeredInfo: javax.jmdns.ServiceInfo? = null

    actual fun startAdvertising(port: Int, serviceName: String) {
        stopAdvertising()
        val instances = createInstances()
        jmdns = instances
        val info = javax.jmdns.ServiceInfo.create(SERVICE_TYPE, serviceName, port, "Llamatik peer")
        registeredInfo = info
        instances.forEach { it.registerService(info) }
    }

    actual fun stopAdvertising() {
        registeredInfo?.let { info -> jmdns.forEach { runCatching { it.unregisterService(info) } } }
        jmdns.forEach { runCatching { it.close() } }
        jmdns = emptyList()
        registeredInfo = null
    }

    actual fun startDiscovering(): Flow<DiscoveryEvent> = callbackFlow {
        val instances = createInstances()

        val listener = object : javax.jmdns.ServiceListener {
            override fun serviceAdded(event: javax.jmdns.ServiceEvent) {
                event.dns.requestServiceInfo(event.type, event.name)
            }
            override fun serviceRemoved(event: javax.jmdns.ServiceEvent) {
                trySend(DiscoveryEvent.NodeLost(event.name))
            }
            override fun serviceResolved(event: javax.jmdns.ServiceEvent) {
                val info = event.info
                val addresses = info.inetAddresses
                val address = (addresses.firstOrNull { it is java.net.Inet4Address }
                    ?: addresses.firstOrNull())?.hostAddress ?: return
                trySend(
                    DiscoveryEvent.NodeFound(
                        DiscoveredNode(
                            id = info.name,
                            host = address,
                            port = info.port,
                            name = info.name,
                        )
                    )
                )
            }
        }

        instances.forEach { it.addServiceListener(SERVICE_TYPE, listener) }

        awaitClose {
            instances.forEach {
                runCatching { it.removeServiceListener(SERVICE_TYPE, listener) }
                runCatching { it.close() }
            }
        }
    }

    actual fun stopDiscovering() {}

    /**
     * Binds one JmDNS per real IPv4 LAN interface. JmDNS.create() with no address can pick a
     * VPN tunnel (e.g. utun) or link-local IPv6 address and then discover nothing.
     */
    private fun createInstances(): List<javax.jmdns.JmDNS> {
        val addresses = runCatching {
            java.net.NetworkInterface.getNetworkInterfaces().toList()
                .filter { runCatching { it.isUp && it.supportsMulticast() && !it.isLoopback && !it.isPointToPoint }.getOrDefault(false) }
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<java.net.Inet4Address>()
                .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
        }.getOrDefault(emptyList())

        val created = addresses.mapNotNull { runCatching { javax.jmdns.JmDNS.create(it) }.getOrNull() }
        return created.ifEmpty { listOf(javax.jmdns.JmDNS.create()) }
    }
}
