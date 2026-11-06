package com.llamatik.network.client.dto

import kotlinx.serialization.Serializable

// ── GET /v1/models response DTOs ──────────────────────────────────────────
// Mirror of the server's com.llamatik.api.*Dto classes. Field names and
// nullability must match exactly so kotlinx-serialization can decode them.

/** Status of a single-file bridge (embedding, whisper, MTP). */
@Serializable
data class BridgeStatusDto(
    val loaded: Boolean,
    /** Filename of the loaded model, or null when no model is loaded. */
    val model: String? = null,
)

/**
 * Capabilities introspected from a loaded generation model.
 * Null in [GenerationBridgeStatusDto.capabilities] when no model is loaded.
 */
@Serializable
data class GenerationCapabilitiesDto(
    /**
     * Value of the `general.finetune` GGUF metadata field.
     * `"instruct"` / `"chat"` → instruction-tuned model.
     * `"base"` / null → base model, prompt manually.
     */
    val finetuneType: String?,
    /** True when the model has an embedded Jinja chat template. */
    val hasChatTemplate: Boolean,
)

/** Status of the generation (LLM) bridge. */
@Serializable
data class GenerationBridgeStatusDto(
    val loaded: Boolean,
    val model: String? = null,
    /** Non-null when a model is loaded. */
    val capabilities: GenerationCapabilitiesDto? = null,
)

/** Status of the Stable Diffusion bridge. */
@Serializable
data class SdBridgeStatusDto(
    val loaded: Boolean,
    val model: String? = null,
    val vae: String? = null,
    val t5xxl: String? = null,
    val clipVision: String? = null,
    /**
     * True when the loaded SD model supports `txt2video` (e.g. Wan 2.1/2.2).
     * Null when no model is loaded.
     */
    val supportsVideo: Boolean? = null,
)

/** Status of the multimodal / VLM bridge. */
@Serializable
data class MultimodalBridgeStatusDto(
    val loaded: Boolean,
    val model: String? = null,
    val mmproj: String? = null,
)

/** Full snapshot returned by `GET /v1/models`. */
@Serializable
data class ModelsStatusResponse(
    val generation: GenerationBridgeStatusDto,
    val embedding: BridgeStatusDto,
    val whisper: BridgeStatusDto,
    val stableDiffusion: SdBridgeStatusDto,
    val multimodal: MultimodalBridgeStatusDto,
    val mtp: BridgeStatusDto,
)
