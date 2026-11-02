package com.llamatik.app.platform

actual object VideoSaver {
    actual suspend fun saveVideo(frames: List<ByteArray>, width: Int, height: Int, fps: Int): Boolean = false
}
