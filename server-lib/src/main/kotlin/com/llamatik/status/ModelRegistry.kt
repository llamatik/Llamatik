package com.llamatik.status

import java.nio.file.Path

/**
 * In-memory registry of which inference models are currently loaded.
 *
 * Updated by every `/init`, `release`, and `shutdown` route handler, and by
 * [com.llamatik.configureStartupLoading] after a successful startup load.
 *
 * Only the filename is stored (not the full absolute path) so that the
 * `/v1/models` response never leaks server-internal directory structure.
 *
 * All properties are `@Volatile` — concurrent reads from route coroutines are
 * always consistent. Sequential writes (init/release are serialized by the
 * service-level mutex) make a full lock unnecessary.
 */
object ModelRegistry {

    // ── State types ───────────────────────────────────────────────────────────

    /** State for single-file bridges with no model-level capabilities (embedding, whisper, MTP). */
    data class BridgeState(
        val loaded: Boolean,
        val model: String? = null,
    )

    /**
     * State for the generation bridge.
     *
     * [finetuneType] is the `general.finetune` GGUF metadata field (`"instruct"`, `"chat"`,
     * `"base"`, or null for base models).
     * [hasChatTemplate] is true when the model has an embedded Jinja chat template,
     * meaning `applyChatTemplate` can be used instead of raw prompt formatting.
     */
    data class GenerationState(
        val loaded: Boolean,
        val model: String? = null,
        val finetuneType: String? = null,
        val hasChatTemplate: Boolean? = null,
    )

    /**
     * State for the Stable Diffusion bridge.
     *
     * [supportsVideo] is true when the loaded model supports `txt2video` (e.g. Wan 2.1/2.2).
     * It is null when no model is loaded.
     */
    data class SdState(
        val loaded: Boolean,
        val model: String? = null,
        val vae: String? = null,
        val t5xxl: String? = null,
        val clipVision: String? = null,
        val supportsVideo: Boolean? = null,
    )

    data class MultimodalState(
        val loaded: Boolean,
        val model: String? = null,
        val mmproj: String? = null,
    )

    data class MtpState(
        val loaded: Boolean,
        val model: String? = null,
    )

    // ── Current state ─────────────────────────────────────────────────────────

    @Volatile var generation: GenerationState = GenerationState(false)
        private set

    @Volatile var embedding: BridgeState = BridgeState(false)
        private set

    @Volatile var whisper: BridgeState = BridgeState(false)
        private set

    @Volatile var stableDiffusion: SdState = SdState(false)
        private set

    @Volatile var multimodal: MultimodalState = MultimodalState(false)
        private set

    @Volatile var mtp: MtpState = MtpState(false)
        private set

    // ── Mutations ─────────────────────────────────────────────────────────────

    fun setGenerationLoaded(
        modelPath: String,
        finetuneType: String? = null,
        hasChatTemplate: Boolean = false,
    ) {
        generation = GenerationState(
            loaded = true,
            model = fileName(modelPath),
            finetuneType = finetuneType,
            hasChatTemplate = hasChatTemplate,
        )
    }

    fun clearGeneration() {
        generation = GenerationState(false)
    }

    fun setEmbeddingLoaded(modelPath: String) {
        embedding = BridgeState(loaded = true, model = fileName(modelPath))
    }

    fun setWhisperLoaded(modelPath: String) {
        whisper = BridgeState(loaded = true, model = fileName(modelPath))
    }

    fun clearWhisper() {
        whisper = BridgeState(false)
    }

    fun setStableDiffusionLoaded(
        modelPath: String,
        vaePath: String? = null,
        t5xxlPath: String? = null,
        clipVisionPath: String? = null,
        supportsVideo: Boolean = false,
    ) {
        stableDiffusion = SdState(
            loaded = true,
            model = fileName(modelPath),
            vae = vaePath?.let { fileName(it) },
            t5xxl = t5xxlPath?.let { fileName(it) },
            clipVision = clipVisionPath?.let { fileName(it) },
            supportsVideo = supportsVideo,
        )
    }

    fun clearStableDiffusion() {
        stableDiffusion = SdState(false)
    }

    fun setMultimodalLoaded(modelPath: String, mmprojPath: String) {
        multimodal = MultimodalState(
            loaded = true,
            model = fileName(modelPath),
            mmproj = fileName(mmprojPath),
        )
    }

    fun clearMultimodal() {
        multimodal = MultimodalState(false)
    }

    fun setMtpLoaded(modelPath: String) {
        mtp = MtpState(loaded = true, model = fileName(modelPath))
    }

    fun clearMtp() {
        mtp = MtpState(false)
    }

    // ── Test utility ──────────────────────────────────────────────────────────

    /** Resets all state to unloaded. Intended for use in tests only. */
    internal fun reset() {
        generation = GenerationState(false)
        embedding = BridgeState(false)
        whisper = BridgeState(false)
        stableDiffusion = SdState(false)
        multimodal = MultimodalState(false)
        mtp = MtpState(false)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun fileName(path: String): String = Path.of(path).fileName.toString()
}
