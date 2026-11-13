package com.llamatik.stablediffusion

interface StableDiffusionServicePort {
    suspend fun initModel(
        modelPath: String,
        threads: Int,
        vaePath: String? = null,
        t5xxlPath: String? = null,
        clipVisionPath: String? = null,
    ): Result<Boolean>

    suspend fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ): Result<ByteArray>

    suspend fun img2img(
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
    ): Result<ByteArray>

    suspend fun release(): Result<Unit>

    /** Returns true when the currently-loaded model supports video generation (e.g. Wan 2.1/2.2). */
    suspend fun supportsVideoGeneration(): Result<Boolean>

    /**
     * Generate a video from a text prompt.
     *
     * Returns a packed [ByteArray] whose layout matches [StableDiffusionBridge.txt2video]:
     * a 16-byte little-endian header `[frameCount, width, height, fps]` followed by
     * `frameCount × width × height × 4` bytes of raw RGBA pixel data.
     * Returns an empty array on failure.
     */
    suspend fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ): Result<ByteArray>
}
