package com.llamatik.app.feature.server

import com.llamatik.app.feature.chatbot.viewmodel.GenerationMode

data class ServerCapabilities(
    val hasText: Boolean = false,
    val hasImage: Boolean = false,
    val hasVision: Boolean = false,
    val hasVideo: Boolean = false,
    val hasAudio: Boolean = false,
)

data class ConnectedServer(
    val host: String,
    val port: Int,
    val name: String = host,
    val capabilities: ServerCapabilities = ServerCapabilities(),
) {
    fun supportsMode(mode: GenerationMode): Boolean = when (mode) {
        GenerationMode.TEXT -> capabilities.hasText
        GenerationMode.IMAGE -> capabilities.hasImage
        GenerationMode.IMAGE_TO_IMAGE -> capabilities.hasImage
        GenerationMode.VISION -> capabilities.hasVision
        GenerationMode.VIDEO -> capabilities.hasVideo
    }
}

data class SavedServer(
    val host: String,
    val port: Int,
    val name: String = host,
)
