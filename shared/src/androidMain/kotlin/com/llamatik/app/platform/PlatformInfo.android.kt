package com.llamatik.app.platform

actual object PlatformInfo {
        actual val deviceName: String = android.os.Build.MODEL ?: "Android device"
    actual val isWasm: Boolean = false
}