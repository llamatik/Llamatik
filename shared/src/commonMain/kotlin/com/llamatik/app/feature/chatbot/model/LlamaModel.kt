package com.llamatik.app.feature.chatbot.model

import com.llamatik.app.feature.chatbot.utils.Plain
import com.llamatik.app.feature.chatbot.utils.PromptTemplate

enum class ModelSource {
    BundledCatalog,
    DownloadedCatalog,
    UserImported,
    CustomUrlDownload,
}

enum class SdCapability {
    Image,
    Video,
    ImageAndVideo,
}

data class LlamaModel(
    val name: String,
    val url: String,
    val sizeMb: Int,
    val fileName: String? = null,
    val localPath: String? = null,
    val template: PromptTemplate = Plain,
    val systemPrompt: String? = null,
    // VLM-specific: companion mmproj model required for multimodal inference
    val mmprojUrl: String? = null,
    val mmprojFileName: String? = null,
    val mmprojSizeMb: Int = 0,
    val mmprojLocalPath: String? = null,
    val source: ModelSource = ModelSource.BundledCatalog,
    val displayName: String? = null,
    val sizeBytes: Long? = null,
    val quantization: String? = null,
    val importedContextLength: Int? = null,
    val createdAtEpochMs: Long? = null,
    // SD-specific: what this model can generate (null = not an SD model or unknown)
    val sdCapability: SdCapability? = null,
    // Mobile suitability rating 1–4 stars (null = not rated / not applicable)
    val mobileSuitability: Int? = null,
    // SD companion files for video models (VAE, T5 text encoder, CLIP vision)
    val vaeUrl: String? = null,
    val vaeSizeMb: Int = 0,
    val t5xxlUrl: String? = null,
    val t5xxlSizeMb: Int = 0,
    val clipVisionUrl: String? = null,
    val clipVisionSizeMb: Int = 0,
)

val LlamaModel.isVlm: Boolean get() = mmprojUrl != null
val LlamaModel.isUserImported: Boolean get() = source == ModelSource.UserImported
val LlamaModel.totalSizeMb: Int get() = sizeMb + mmprojSizeMb + vaeSizeMb + t5xxlSizeMb + clipVisionSizeMb
