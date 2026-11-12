package com.llamatik.app.platform

import com.llamatik.host.EmbeddedServer
import com.llamatik.host.HostConfig

actual object HostController {
    actual fun start(server: EmbeddedServer, config: HostConfig) = Unit
    actual fun stop(server: EmbeddedServer) = Unit
}
