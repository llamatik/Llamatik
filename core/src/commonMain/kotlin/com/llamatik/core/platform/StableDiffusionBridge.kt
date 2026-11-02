package com.llamatik.core.platform

/**
 * Minimal Stable Diffusion bridge backed by leejet/stable-diffusion.cpp.
 *
 * Mirrors the existing LlamaBridge / WhisperBridge pattern:
 * - Android/JVM: JNI via libllama_jni
 * - iOS: Kotlin/Native cinterop
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object StableDiffusionBridge {

    fun getModelPath(modelFileName: String): String

    /**
     * Initializes a Stable Diffusion context using a single model file.
     *
     * @param modelPath Absolute path to a .safetensors/.ckpt/.gguf model.
     * @param threads Number of CPU threads to use. Use -1 for default.
     */
    fun initModel(
        modelPath: String,
        threads: Int = -1,
        vaePath: String? = null,
        t5xxlPath: String? = null,
        clipVisionPath: String? = null,
    ): Boolean

    /**
     * Generates a single RGBA image (width * height * 4 bytes).
     *
     * @return RGBA byte array or an empty array on failure.
     */
    fun txt2img(
        prompt: String,
        negativePrompt: String? = null,
        width: Int = 512,
        height: Int = 512,
        steps: Int = 20,
        cfgScale: Float = 7.0f,
        seed: Long = -1L,
    ): ByteArray

    /**
     * Image-to-image generation. Modifies [initImageRgba] (RGBA, [initImageW]×[initImageH]×4 bytes)
     * according to [prompt]. [strength] controls how much the original is altered (0.0 = unchanged,
     * 1.0 = fully replaced by prompt).
     *
     * @return RGBA byte array or an empty array on failure.
     */
    fun img2img(
        initImageRgba: ByteArray,
        initImageW: Int,
        initImageH: Int,
        prompt: String,
        negativePrompt: String? = null,
        width: Int = 512,
        height: Int = 512,
        steps: Int = 20,
        cfgScale: Float = 7.0f,
        strength: Float = 0.75f,
        seed: Long = -1L,
    ): ByteArray

    fun release()

    /**
     * Returns true if the currently loaded model supports video generation.
     * Must be called after [initModel].
     */
    fun supportsVideoGeneration(): Boolean

    /**
     * Generates video frames from a text prompt.
     *
     * @return Packed byte array: 16-byte LE header [frameCount, width, height, fps]
     *         followed by frameCount * (width * height * 4) RGBA bytes.
     *         Empty array on failure.
     */
    fun txt2video(
        prompt: String,
        negativePrompt: String? = null,
        width: Int = 512,
        height: Int = 512,
        videoFrames: Int = 16,
        fps: Int = 8,
        cfgScale: Float = 7.0f,
        seed: Long = -1L,
    ): ByteArray
}
