package com.llamatik.app.platform

import com.llamatik.host.EmbeddedServer
import com.llamatik.host.HostConfig

actual object HostController {
    actual fun start(server: EmbeddedServer, config: HostConfig) = server.start(config)
    actual fun stop(server: EmbeddedServer) = server.stop()
}
