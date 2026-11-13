package com.llamatik.network.client

data class LlamatikServerConfig(
    val host: String,
    val port: Int = 8080,
    /** Per-device token issued by a server that requires approval; sent as `Authorization: Bearer`. */
    val accessToken: String? = null,
) {
    val baseUrl: String get() = "http://$host:$port/v1"
}
