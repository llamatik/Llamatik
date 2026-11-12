package com.llamatik.app.platform

actual object PlatformInfo {
        actual val deviceName: String = runCatching { java.net.InetAddress.getLocalHost().hostName }.getOrDefault("Desktop")
    actual val isWasm: Boolean = false
}