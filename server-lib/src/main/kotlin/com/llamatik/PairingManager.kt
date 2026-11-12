package com.llamatik

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

/** A device that asked for access and is waiting for the host's decision. */
data class PairingRequest(val id: String, val clientId: String, val clientName: String, val createdAtMillis: Long)

/** A device the host has approved. [token] is secret and only ever sent to that device. */
data class ApprovedDevice(val clientId: String, val name: String, val token: String, val lastSeenMillis: Long)

/** Persistence for approved devices, so approvals survive restarts. Implemented by the host. */
interface DeviceStore {
    fun load(): List<ApprovedDevice>
    fun save(devices: List<ApprovedDevice>)
}

/**
 * Per-device approval. A new device calls `POST /v1/pairing/request`, the host shows
 * [pending] to the user and calls [approve] or [deny]; the device polls
 * `GET /v1/pairing/{id}` and receives its own revocable token once approved.
 */
class PairingManager(
    private val store: DeviceStore? = null,
    private val requestTtlMillis: Long = 60_000,
    private val maxPending: Int = 5,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    enum class Status { PENDING, APPROVED, DENIED, EXPIRED }

    private val lock = Any()
    private val random = SecureRandom()
    private val _pending = MutableStateFlow<List<PairingRequest>>(emptyList())
    private val _devices = MutableStateFlow(store?.load().orEmpty())
    private val outcomes = HashMap<String, Pair<Status, String?>>()

    val pending: StateFlow<List<PairingRequest>> get() = _pending
    val devices: StateFlow<List<ApprovedDevice>> get() = _devices

    /** Returns the request id, or null when too many requests are already waiting. */
    fun request(clientId: String, clientName: String): String? = synchronized(lock) {
        expire()
        _pending.value.firstOrNull { it.clientId == clientId }?.let { return it.id }
        if (_pending.value.size >= maxPending) return null
        val req = PairingRequest(UUID.randomUUID().toString(), clientId, clientName.take(64), clock())
        _pending.value = _pending.value + req
        req.id
    }

    fun approve(requestId: String): Boolean = synchronized(lock) {
        val req = _pending.value.firstOrNull { it.id == requestId } ?: return false
        val device = ApprovedDevice(req.clientId, req.clientName, newToken(), clock())
        _devices.value = _devices.value.filterNot { it.clientId == req.clientId } + device
        _pending.value = _pending.value - req
        outcomes[requestId] = Status.APPROVED to device.token
        persist()
        true
    }

    fun deny(requestId: String): Boolean = synchronized(lock) {
        val req = _pending.value.firstOrNull { it.id == requestId } ?: return false
        _pending.value = _pending.value - req
        outcomes[requestId] = Status.DENIED to null
        true
    }

    fun revoke(clientId: String) = synchronized(lock) {
        _devices.value = _devices.value.filterNot { it.clientId == clientId }
        persist()
    }

    /** Status of a request; the token is returned once, on the first poll after approval. */
    fun poll(requestId: String): Pair<Status, String?> = synchronized(lock) {
        expire()
        if (_pending.value.any { it.id == requestId }) return Status.PENDING to null
        val outcome = outcomes[requestId] ?: return Status.EXPIRED to null
        if (outcome.first == Status.APPROVED) outcomes[requestId] = Status.APPROVED to null
        outcome
    }

    /** Resolves a bearer token to its device (and records last-seen), or null. */
    fun authenticate(token: String): ApprovedDevice? = synchronized(lock) {
        val match = _devices.value.firstOrNull { constantTimeEquals(it.token, token) } ?: return null
        match.copy(lastSeenMillis = clock()).also { updated ->
            _devices.value = _devices.value.map { if (it.clientId == match.clientId) updated else it }
        }
    }

    private fun expire() {
        val now = clock()
        val (live, dead) = _pending.value.partition { now - it.createdAtMillis < requestTtlMillis }
        if (dead.isNotEmpty()) {
            _pending.value = live
            dead.forEach { outcomes[it.id] = Status.EXPIRED to null }
        }
    }

    private fun persist() = store?.save(_devices.value)

    private fun newToken(): String =
        ByteArray(24).also(random::nextBytes).let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        java.security.MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}

@Serializable
data class PairingRequestBody(val clientId: String, val clientName: String)

@Serializable
data class PairingRequestResponse(val requestId: String)

@Serializable
data class PairingStatusResponse(val status: String, val token: String? = null)
