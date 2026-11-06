package com.llamatik.network.client.dto

/**
 * Synthesised capabilities for a connected Llamatik server.
 * This is NOT a JSON response — it is built by [LlamatikServerClient.getCapabilities]
 * by probing the individual service endpoints that do exist on the server.
 */
data class ServerCapabilitiesResponse(
    val text: Boolean = false,
    val image: Boolean = false,
    val vision: Boolean = false,
    val video: Boolean = false,
    val audio: Boolean = false,
)
