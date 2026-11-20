package com.llamatik.app.platform

import io.github.vinceglb.filekit.PlatformFile

/**
 * Local-disk helpers that FileKit only provides on native/JVM targets. On the web there is no
 * addressable filesystem, so these report "nothing" and local model import stays disabled there.
 */
expect fun localFileExists(path: String): Boolean

/** Absolute path of [file], or an empty string when the platform has no filesystem paths. */
expect fun PlatformFile.localAbsolutePath(): String
