package com.llamatik.fakes

import com.llamatik.core.platform.GenStream
import com.llamatik.multimodal.MultimodalServicePort

class FakeMultimodalService(
    private val initResult: Result<Boolean> = Result.success(true),
    private val cancelResult: Result<Unit> = Result.success(Unit),
    private val releaseResult: Result<Unit> = Result.success(Unit),
    private val streamDeltas: List<String> = listOf("a cat"),
) : MultimodalServicePort {
    override suspend fun initModel(modelPath: String, mmprojPath: String) = initResult
    override fun analyzeImageBytesStream(imageBytes: ByteArray, prompt: String, callback: GenStream) {
        streamDeltas.forEach { callback.onDelta(it) }
        callback.onComplete()
    }
    override suspend fun cancelAnalysis() = cancelResult
    override suspend fun release() = releaseResult
}
