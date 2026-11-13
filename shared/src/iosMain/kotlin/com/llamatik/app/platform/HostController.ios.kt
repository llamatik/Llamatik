package com.llamatik.app.platform

import com.llamatik.host.EmbeddedServer
import com.llamatik.host.HostConfig
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskIdentifier
import platform.UIKit.UIBackgroundTaskInvalid

/**
 * iOS suspends apps shortly after they leave the foreground, and a suspended app cannot serve
 * requests (iOS has no equivalent of an Android foreground service for a network server). So while
 * sharing we keep the screen from auto-locking and ask for the little extra background time iOS
 * grants; the server is fully reachable while the app is on screen.
 */
actual object HostController {
    private var backgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid

    actual fun start(server: EmbeddedServer, config: HostConfig) {
        UIApplication.sharedApplication.idleTimerDisabled = true
        beginBackgroundTask()
        server.start(config)
    }

    actual fun stop(server: EmbeddedServer) {
        server.stop()
        endBackgroundTask()
        UIApplication.sharedApplication.idleTimerDisabled = false
    }

    private fun beginBackgroundTask() {
        endBackgroundTask()
        backgroundTask = UIApplication.sharedApplication.beginBackgroundTaskWithExpirationHandler {
            // Time is up: iOS will suspend the app, so release the task rather than being killed.
            endBackgroundTask()
        }
    }

    private fun endBackgroundTask() {
        if (backgroundTask != UIBackgroundTaskInvalid) {
            UIApplication.sharedApplication.endBackgroundTask(backgroundTask)
            backgroundTask = UIBackgroundTaskInvalid
        }
    }
}
