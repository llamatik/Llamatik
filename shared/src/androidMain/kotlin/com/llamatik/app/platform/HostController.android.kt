package com.llamatik.app.platform

import com.llamatik.host.EmbeddedServer
import com.llamatik.host.EmbeddedServerService
import com.llamatik.host.HostConfig

actual object HostController {
    actual fun start(server: EmbeddedServer, config: HostConfig) {
        EmbeddedServerService.start(AndroidContextHolder.appContext, config)
    }

    actual fun stop(server: EmbeddedServer) {
        // The service stops the server, releases its locks and removes the notification.
        EmbeddedServerService.stop(AndroidContextHolder.appContext)
    }
}
