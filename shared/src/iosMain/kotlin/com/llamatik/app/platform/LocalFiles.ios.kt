package com.llamatik.app.platform

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.exists

actual fun localFileExists(path: String): Boolean = runCatching { PlatformFile(path).exists() }.getOrDefault(false)

actual fun PlatformFile.localAbsolutePath(): String = absolutePath()
