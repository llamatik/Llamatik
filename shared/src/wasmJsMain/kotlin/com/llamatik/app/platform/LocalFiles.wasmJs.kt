package com.llamatik.app.platform

import io.github.vinceglb.filekit.PlatformFile

actual fun localFileExists(path: String): Boolean = false

actual fun PlatformFile.localAbsolutePath(): String = ""
