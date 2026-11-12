package com.llamatik.core.platform

/**
 * What the loaded multimodal model can do, as reported by llama.cpp's
 * `mtmd_helper_get_capabilities()`.
 */
data class MultimodalCapabilities(
    /** False for TTS-only models and realtime transcription models. */
    val chat: Boolean = false,
    val vision: Boolean = false,
    val audio: Boolean = false,
    val video: Boolean = false,
    /** Realtime audio transcription. */
    val audioStream: Boolean = false,
    /** False for audio-only models. */
    val textGen: Boolean = false,
    val audioGen: Boolean = false,
    /** Accepts a speaker reference audio for voice cloning. */
    val audioGenSpeakerRef: Boolean = false,
) {
    companion object {
        val NONE = MultimodalCapabilities()

        /** Decodes the bitmask produced by the native layer. */
        fun fromMask(mask: Int): MultimodalCapabilities = MultimodalCapabilities(
            chat = mask and (1 shl 0) != 0,
            vision = mask and (1 shl 1) != 0,
            audio = mask and (1 shl 2) != 0,
            video = mask and (1 shl 3) != 0,
            audioStream = mask and (1 shl 4) != 0,
            textGen = mask and (1 shl 5) != 0,
            audioGen = mask and (1 shl 6) != 0,
            audioGenSpeakerRef = mask and (1 shl 7) != 0,
        )
    }
}
