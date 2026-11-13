package com.llamatik.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.security.SecureRandom
import java.util.concurrent.locks.ReentrantLock

internal actual class Lock actual constructor() {
    private val delegate = ReentrantLock()
    actual fun lock() = delegate.lock()
    actual fun unlock() = delegate.unlock()
}

private val random = SecureRandom()

internal actual val blockingDispatcher: CoroutineDispatcher = Dispatchers.IO

internal actual fun secureRandomBytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)

internal actual fun envVar(name: String): String? = System.getenv(name)

internal actual val pathSeparator: String = File.separator

internal actual fun normalizeAbsolutePath(raw: String): String =
    try {
        Path.of(raw).toAbsolutePath().normalize().toString()
    } catch (e: InvalidPathException) {
        throw IllegalArgumentException("Invalid path", e)
    }

internal actual fun <E> SendChannel<E>.sendBlocking(element: E): Boolean = trySendBlocking(element).isSuccess
