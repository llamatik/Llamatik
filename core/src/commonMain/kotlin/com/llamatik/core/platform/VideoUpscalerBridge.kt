package com.llamatik.core.platform

/**
 * Video super-resolution backed by stable-diffusion.cpp (RelayVSR / SeedVR2 style models).
 *
 * Independent from [StableDiffusionBridge]: it owns its own native context, so it can be
 * used while a generation model is loaded or after it has been released.
 *
 * Frames use the same packed layout as [StableDiffusionBridge.txt2video]:
 * a 16-byte little-endian header `[frameCount, width, height, fps]` followed by
 * `frameCount * width * height * 4` RGBA bytes.
 *
 * Not available on WASM (all calls fail / return empty).
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object VideoUpscalerBridge {

    /**
     * Loads the upscaler.
     *
     * @param diffusionModelPath Upscaler diffusion model (GGUF/safetensors).
     * @param vaePath VAE matching the diffusion model.
     * @param upscaleFactor 2 or 4. RelayVSR requires it to match both checkpoints.
     * @param threads CPU threads, -1 for default.
     */
    fun initModel(
        diffusionModelPath: String,
        vaePath: String,
        upscaleFactor: Int = 2,
        threads: Int = -1,
    ): Boolean

    /** True after a successful [initModel]. */
    fun isLoaded(): Boolean

    /**
     * Upscales a packed frame sequence (see class docs). Blocks until done; call from a background thread.
     *
     * @param seed Seed for the upscaler; negative keeps the library default.
     * @param windowSize RelayVSR reference window including both endpoints; 0 uses the library default (16).
     * @param temporalChunkSize SeedVR2 independent temporal chunk in input frames (1 or 4n+1); 0 uses the default (5).
     * @return Packed upscaled frames in the same layout, or an empty array on failure.
     */
    fun upscale(
        packedFrames: ByteArray,
        seed: Long = -1L,
        windowSize: Int = 0,
        temporalChunkSize: Int = 0,
    ): ByteArray

    fun release()
}
