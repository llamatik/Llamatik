package com.llamatik.core.platform

actual object StableDiffusionBridge {
    init {
        // Single native shared library for all JNI bridges (llama/whisper/sd).
        System.loadLibrary("llama_jni")
    }

    actual fun getModelPath(modelFileName: String): String = modelFileName

    actual fun initModel(
        modelPath: String, threads: Int,
        vaePath: String?, t5xxlPath: String?, clipVisionPath: String?,
    ): Boolean = initModelNative(modelPath, threads, vaePath, t5xxlPath, clipVisionPath)

    private external fun initModelNative(
        modelPath: String, threads: Int,
        vaePath: String?, t5xxlPath: String?, clipVisionPath: String?,
    ): Boolean

    actual external fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray

    actual external fun img2img(
        initImageRgba: ByteArray,
        initImageW: Int,
        initImageH: Int,
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        strength: Float,
        seed: Long,
    ): ByteArray

    actual external fun release()

    actual external fun supportsVideoGeneration(): Boolean

    actual external fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray
}
