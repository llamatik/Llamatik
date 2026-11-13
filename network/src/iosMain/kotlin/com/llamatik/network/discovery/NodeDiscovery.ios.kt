package com.llamatik.network.discovery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import platform.Foundation.NSNetService
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual object NodeDiscovery {
    // Same service type the JVM and Android implementations advertise (`_llamatik._tcp`).
    private const val SERVICE_TYPE = "_llamatik._tcp."
    private var service: NSNetService? = null

    /** Publishes this device over Bonjour so other Llamatik devices can find it. */
    actual fun startAdvertising(port: Int, serviceName: String) {
        stopAdvertising()
        // NSNetService publishes on the run loop of the thread that starts it; only the main thread has one.
        dispatch_async(dispatch_get_main_queue()) {
            service = NSNetService(domain = "", type = SERVICE_TYPE, name = serviceName, port = port).also {
                it.publish()
            }
        }
    }

    actual fun stopAdvertising() {
        dispatch_async(dispatch_get_main_queue()) {
            service?.stop()
            service = null
        }
    }

    actual fun startDiscovering(): Flow<DiscoveryEvent> = emptyFlow()
    actual fun stopDiscovering() {}
}
