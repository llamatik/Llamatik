package com.llamatik.core.platform

actual object VideoUpscalerBridge {
    init {
        // Single native shared library for all JNI bridges (llama/whisper/sd).
        System.loadLibrary("llama_jni")
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
