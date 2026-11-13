package com.llamatik.network.client.dto

import kotlinx.serialization.Serializable

// Wire format of the server's per-device approval endpoints (/v1/pairing/...).

@Serializable
data class PairingRequestBody(val clientId: String, val clientName: String)

@Serializable
data class PairingRequestResponse(val requestId: String)

/** [status] is one of `pending`, `approved`, `denied`, `expired`. */
@Serializable
data class PairingStatusResponse(val status: String, val token: String? = null)
