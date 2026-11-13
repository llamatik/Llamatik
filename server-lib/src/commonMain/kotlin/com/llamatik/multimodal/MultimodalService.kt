package com.llamatik.multimodal

import com.llamatik.core.platform.GenStream
import com.llamatik.core.platform.MultimodalBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MultimodalService : MultimodalServicePort {

    private val mutex = Mutex()

    override suspend fun initModel(modelPath: String, mmprojPath: String): Result<Boolean> = runCatching {
        mutex.withLock { MultimodalBridge.initModel(modelPath, mmprojPath) }
    }

    override fun analyzeImageBytesStream(imageBytes: ByteArray, prompt: String, callback: GenStream) {
        MultimodalBridge.analyzeImageBytesStream(imageBytes, prompt, callback)
    }

    override suspend fun cancelAnalysis(): Result<Unit> = runCatching {
        mutex.withLock { MultimodalBridge.cancelAnalysis() }
    }

    override suspend fun release(): Result<Unit> = runCatching {
        mutex.withLock { MultimodalBridge.release() }
    }
}
