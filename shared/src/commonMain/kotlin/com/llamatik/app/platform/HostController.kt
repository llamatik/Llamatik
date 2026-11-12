package com.llamatik.app.platform

import com.llamatik.host.EmbeddedServer
import com.llamatik.host.HostConfig

/**
 * Starts/stops the embedded server the platform-appropriate way: Android runs it inside a
 * foreground service so it survives the app going to the background; desktop runs it in-process.
 */
expect object HostController {
    fun start(server: EmbeddedServer, config: HostConfig)
    fun stop(server: EmbeddedServer)
}
