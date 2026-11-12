package com.llamatik.core.platform

actual object VideoUpscalerBridge {
    init {
        // The native library is shared with StableDiffusionBridge; its initializer extracts
        // and loads it once, so touching it here guarantees the JNI symbols are bound.
        StableDiffusionBridge.ensureNativeLoaded()
    }

    actual fun initModel(
        diffusionModelPath: String,
        vaePath: String,
        upscaleFactor: Int,
        threads: Int,
    ): Boolean = initModelNative(diffusionModelPath, vaePath, upscaleFactor, threads)

    private external fun initModelNative(
        diffusionModelPath: String,
        vaePath: String,
        upscaleFactor: Int,
        threads: Int,
    ): Boolean

    actual external fun isLoaded(): Boolean

    actual external fun upscale(
        packedFrames: ByteArray,
        seed: Long,
        windowSize: Int,
        temporalChunkSize: Int,
    ): ByteArray

    actual external fun release()
}
