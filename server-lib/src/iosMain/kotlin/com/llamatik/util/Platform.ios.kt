@file:OptIn(ExperimentalForeignApi::class)

package com.llamatik.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSRecursiveLock
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.posix.getenv

internal actual class Lock actual constructor() {
    private val delegate = NSRecursiveLock()
    actual fun lock() = delegate.lock()
    actual fun unlock() = delegate.unlock()
}

internal actual val blockingDispatcher: CoroutineDispatcher = Dispatchers.IO

internal actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    val status = bytes.usePinned { SecRandomCopyBytes(kSecRandomDefault, size.toULong(), it.addressOf(0)) }
    check(status == errSecSuccess) { "Secure random generation failed ($status)" }
    return bytes
}

internal actual fun envVar(name: String): String? = getenv(name)?.toKString()

internal actual val pathSeparator: String = "/"

internal actual fun normalizeAbsolutePath(raw: String): String {
    val absolute = if (raw.startsWith("/")) raw else NSFileManager.defaultManager.currentDirectoryPath + "/" + raw
    val segments = ArrayList<String>()
    for (part in absolute.split('/')) {
        when (part) {
            "", "." -> Unit
            ".." -> if (segments.isNotEmpty()) segments.removeAt(segments.lastIndex)
            else -> segments.add(part)
        }
    }
    return "/" + segments.joinToString("/")
}

internal actual fun <E> SendChannel<E>.sendBlocking(element: E): Boolean {
    if (trySend(element).isSuccess) return true
    return runBlocking {
        try {
            send(element)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            false
        }
    }
}
