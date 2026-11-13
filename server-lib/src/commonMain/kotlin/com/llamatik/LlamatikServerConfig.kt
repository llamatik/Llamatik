package com.llamatik

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val API_VERSION = "/v1"

/** Groups of endpoints a host can enable. */
enum class ServerFeature { TEXT, EMBEDDINGS, WHISPER, IMAGE, VIDEO, VISION }

/**
 * Configuration for [installLlamatikServer].
 *
 * The standalone server maps environment variables onto this; the Llamatik app builds it in code.
 *
 * @property features endpoint groups that are served. Disabled groups answer `404`.
 * @property allowAdminEndpoints when false, endpoints that change host state (model init / release /
 *   shutdown, generation params, KV-session save/load, MTP) answer `403`. An embedding host that
 *   shares *its own* loaded models must keep this false so a remote client cannot unload them.
 * @property modelsDir / [sessionsDir] filesystem containment for caller-supplied paths
 *   (see [com.llamatik.util.PathValidator]); `null` keeps the environment-derived defaults.
 * @property isBusy returns true while the host itself is using the inference bridge; inference
 *   requests then get `503` + `Retry-After` instead of interleaving with the host's own generation.
 * @property pairing when set, every endpoint except `GET /` and `/v1/pairing/{id}` requires a
 *   per-device bearer token issued after the host approves the device.
 * @property metrics live request counters, updated for every request.
 */
class LlamatikServerConfig(
    val features: Set<ServerFeature> = ServerFeature.entries.toSet(),
    val allowAdminEndpoints: Boolean = true,
    val modelsDir: String? = null,
    val sessionsDir: String? = null,
    val isBusy: () -> Boolean = { false },
    val pairing: PairingManager? = null,
    val metrics: ServerMetrics = ServerMetrics(),
)

/** Live request counters for a hosting UI. */
class ServerMetrics {
    private val _active = MutableStateFlow(0)
    private val _total = MutableStateFlow(0L)
    val activeRequests: StateFlow<Int> get() = _active
    val totalRequests: StateFlow<Long> get() = _total

    internal fun started() {
        _active.value += 1
        _total.value += 1
    }

    internal fun finished() {
        _active.value -= 1
    }
}
