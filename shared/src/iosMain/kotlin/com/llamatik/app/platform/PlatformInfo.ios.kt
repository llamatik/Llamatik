package com.llamatik.app.platform

actual object PlatformInfo {
        actual val deviceName: String = platform.UIKit.UIDevice.currentDevice.name
    actual val isWasm: Boolean = false
}