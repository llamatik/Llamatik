package com.llamatik.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitModelRequest(
    val modelPath: String
)

@Serializable
data class InitModelResponse(
    val ok: Boolean
)

@Serializable
data class EmbedRequest(
    val input: String
)

@Serializable
data class EmbedResponse(
    val embedding: List<Float>
)

@Serializable
data class GenerateRequest(
    val prompt: String
)

@Serializable
data class GenerateWithContextRequest(
    val systemPrompt: String,
    val contextBlock: String,
    val userPrompt: String
)

@Serializable
data class GenerateJsonRequest(
    val prompt: String,
    val jsonSchema: String? = null
)

@Serializable
data class GenerateJsonWithContextRequest(
    val systemPrompt: String,
    val contextBlock: String,
    val userPrompt: String,
    val jsonSchema: String? = null
)

@Serializable
data class GenerateResponse(
    val text: String
)

@Serializable
data class UpdateParamsRequest(
    val temperature: Float,
    val maxTokens: Int,
    @SerialName("topP") val topP: Float,
    @SerialName("topK") val topK: Int,
    val repeatPenalty: Float,
    val contextLength: Int = 4096,
    val numThreads: Int = 4,
    val useMmap: Boolean = true,
    val flashAttention: Boolean = false,
    val batchSize: Int = 512,
    val gpuLayers: Int = 0,
)

@Serializable
data class OkResponse(
    val ok: Boolean = true
)

// --- session ---

@Serializable
data class SessionSaveRequest(val path: String)

@Serializable
data class SessionLoadRequest(val path: String)

@Serializable
data class SessionResult(val ok: Boolean)

// --- generateContinue ---

@Serializable
data class GenerateContinueRequest(val prompt: String)

// --- model metadata ---

@Serializable
data class ModelFinetuneTypeResponse(val finetuneType: String?)

@Serializable
data class ModelChatTemplateResponse(val chatTemplate: String?)

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ApplyChatTemplateRequest(
    val messages: List<ChatMessage>,
    val addAssistantPrefix: Boolean = true
)

@Serializable
data class ApplyChatTemplateResponse(val prompt: String?)

// --- Whisper (STT) ---

@Serializable
data class WhisperInitRequest(val modelPath: String)

@Serializable
data class TranscribeRequest(
    val wavPath: String,
    val language: String? = null,
    val initialPrompt: String? = null
)

@Serializable
data class TranscribeResponse(val text: String)

// --- Stable Diffusion ---

@Serializable
data class StableDiffusionInitRequest(
    val modelPath: String,
    val threads: Int = -1,
    /** VAE companion model (required for video models such as Wan 2.1/2.2). */
    val vaePath: String? = null,
    /** T5-XXL text encoder companion (required for some video models). */
    val t5xxlPath: String? = null,
    /** CLIP vision encoder companion (required for image-to-video variants). */
    val clipVisionPath: String? = null,
)

@Serializable
data class Txt2ImgRequest(
    val prompt: String,
    val negativePrompt: String? = null,
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.0f,
    val seed: Long = -1L,
)

@Serializable
data class Img2ImgRequest(
    val initImageBase64: String,
    val initImageW: Int,
    val initImageH: Int,
    val prompt: String,
    val negativePrompt: String? = null,
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.0f,
    val strength: Float = 0.75f,
    val seed: Long = -1L,
)

@Serializable
data class ImageResponse(val imageBase64: String)

/** Response for [POST /v1/stablediffusion/txt2video].
 *
 * [videoBase64] is the Base64 encoding of the packed binary frame buffer produced by
 * [StableDiffusionBridge.txt2video]. The binary layout is:
 *
 * ```
 * [frameCount: Int32 LE][width: Int32 LE][height: Int32 LE][fps: Int32 LE]
 * [frame_0_rgba: width * height * 4 bytes]
 * [frame_1_rgba: width * height * 4 bytes]
 * ...
 * ```
 */
@Serializable
data class VideoResponse(val videoBase64: String)

/** Response for [GET /v1/stablediffusion/supportsVideo]. */
@Serializable
data class SupportsVideoResponse(val supportsVideo: Boolean)

/** Request body for [POST /v1/stablediffusion/txt2video]. */
@Serializable
data class Txt2VideoRequest(
    val prompt: String,
    val negativePrompt: String? = null,
    val width: Int = 512,
    val height: Int = 512,
    val videoFrames: Int = 16,
    val fps: Int = 8,
    val cfgScale: Float = 7.0f,
    val seed: Long = -1L,
)

// --- MTP (Multi-Token Prediction / speculative decoding) ---

@Serializable
data class InitMtpRequest(val modelPath: String, val draftLen: Int = 3)

// --- Whisper segments ---

@Serializable
data class TranscribeSegmentsRequest(
    val wavPath: String,
    val language: String? = null,
    val initialPrompt: String? = null,
    val translate: Boolean = false,
    val diarize: Boolean = false,
)

@Serializable
data class TranscribeSegmentsResponse(val json: String)

// --- Multimodal (VLM) ---

@Serializable
data class MultimodalInitRequest(val modelPath: String, val mmprojPath: String)

@Serializable
data class AnalyzeImageRequest(
    val imageBase64: String,
    val prompt: String
)

// --- Models status (GET /v1/models) ---

/** Status of a single-file bridge with no model-level capabilities (embedding, whisper, MTP). */
@Serializable
data class BridgeStatusDto(
    val loaded: Boolean,
    /** Filename of the loaded model, or null when no model is loaded. */
    val model: String? = null,
)

/**
 * Capabilities introspected from a loaded generation model.
 * Null in the parent DTO when no model is loaded.
 */
@Serializable
data class GenerationCapabilitiesDto(
    /**
     * Value of the `general.finetune` GGUF metadata field.
     * `"instruct"` / `"chat"` → instruction-tuned, use system/user prompt formatting.
     * `"base"` / null → base model, prompt manually.
     */
    val finetuneType: String?,
    /**
     * True when the model has an embedded Jinja chat template.
     * When true, `POST /v1/generation/model/applyChatTemplate` can render messages
     * into a prompt string automatically.
     */
    val hasChatTemplate: Boolean,
)

/** Status of the generation bridge including model capabilities. */
@Serializable
data class GenerationBridgeStatusDto(
    val loaded: Boolean,
    val model: String? = null,
    /** Non-null when a model is loaded. */
    val capabilities: GenerationCapabilitiesDto? = null,
)

/** Status of the Stable Diffusion bridge, which may have companion files. */
@Serializable
data class SdBridgeStatusDto(
    val loaded: Boolean,
    val model: String? = null,
    val vae: String? = null,
    val t5xxl: String? = null,
    val clipVision: String? = null,
    /**
     * True when the loaded model supports `txt2video` (e.g. Wan 2.1/2.2).
     * Null when no model is loaded.
     * Saves clients a separate `GET /v1/stablediffusion/supportsVideo` call.
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

/** Full snapshot of every bridge's loaded state and capabilities. */
@Serializable
data class ModelsStatusResponse(
    val generation: GenerationBridgeStatusDto,
    val embedding: BridgeStatusDto,
    val whisper: BridgeStatusDto,
    val stableDiffusion: SdBridgeStatusDto,
    val multimodal: MultimodalBridgeStatusDto,
    val mtp: BridgeStatusDto,
)
