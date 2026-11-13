package com.llamatik.app.platform

expect object PlatformInfo {
    /** Human readable name of this device, shown to other devices (e.g. "Pixel 8"). */
    val deviceName: String
    val isWasm: Boolean
}
