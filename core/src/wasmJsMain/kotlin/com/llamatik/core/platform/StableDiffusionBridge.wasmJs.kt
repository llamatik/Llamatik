package com.llamatik.core.platform

actual object StableDiffusionBridge {
    actual fun getModelPath(modelFileName: String): String = "/models/$modelFileName"
    actual fun initModel(modelPath: String, threads: Int, vaePath: String?, t5xxlPath: String?, clipVisionPath: String?): Boolean = false
    actual fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long
    ): ByteArray = ByteArray(0)
    actual fun img2img(
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
    ): ByteArray = ByteArray(0)
    actual fun release() {}
    actual fun supportsVideoGeneration(): Boolean = false
    actual fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray = ByteArray(0)
}