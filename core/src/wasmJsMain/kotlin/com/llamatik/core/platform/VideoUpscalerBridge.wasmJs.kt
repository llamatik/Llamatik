package com.llamatik.core.platform

/** Video upscaling needs the native backend; unavailable on WASM. */
actual object VideoUpscalerBridge {
    actual fun initModel(diffusionModelPath: String, vaePath: String, upscaleFactor: Int, threads: Int): Boolean = false
    actual fun isLoaded(): Boolean = false
    actual fun upscale(packedFrames: ByteArray, seed: Long, windowSize: Int, temporalChunkSize: Int): ByteArray = ByteArray(0)
    actual fun release() {}
}
