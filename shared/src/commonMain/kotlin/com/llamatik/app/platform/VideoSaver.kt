package com.llamatik.app.platform

expect object VideoSaver {
    suspend fun saveVideo(frames: List<ByteArray>, width: Int, height: Int, fps: Int): Boolean
}
