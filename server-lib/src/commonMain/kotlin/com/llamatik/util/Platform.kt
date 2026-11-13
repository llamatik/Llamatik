package com.llamatik.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.SendChannel

/** Re-entrant mutual exclusion that works on every target (the JVM-only `synchronized` is not common). */
internal expect class Lock() {
    fun lock()
    fun unlock()
}

internal inline fun <T> Lock.withLock(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}

/** Dispatcher for blocking native inference calls (`Dispatchers.IO` is not part of common code). */
internal expect val blockingDispatcher: CoroutineDispatcher

/** Cryptographically secure random bytes (used for device tokens). */
internal expect fun secureRandomBytes(size: Int): ByteArray

/** Value of an environment variable, or null. */
internal expect fun envVar(name: String): String?

/** Separator used when joining path segments on this platform. */
internal expect val pathSeparator: String

/**
 * Resolves [raw] to an absolute, normalized path (no `.`/`..` segments).
 * Throws [IllegalArgumentException] when the path is not valid on this platform.
 */
internal expect fun normalizeAbsolutePath(raw: String): String

/**
 * Sends [element], blocking the calling (non-coroutine, native inference) thread until there is room
 * in the channel. Returns false if the channel is closed. Gives native generation callbacks backpressure against a slow HTTP client.
 */
internal expect fun <E> SendChannel<E>.sendBlocking(element: E): Boolean
