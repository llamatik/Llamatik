package com.llamatik.whisper

import com.llamatik.core.platform.WhisperBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WhisperService : WhisperServicePort {

    private val mutex = Mutex()

    override suspend fun initModel(modelPath: String): Result<Boolean> = runCatching {
        mutex.withLock { WhisperBridge.initModel(modelPath) }
    }

    override suspend fun transcribeWav(wavPath: String, language: String?, initialPrompt: String?): Result<String> =
        runCatching {
            mutex.withLock { WhisperBridge.transcribeWav(wavPath, language, initialPrompt) }
        }

    override suspend fun transcribeWavSegments(wavPath: String, language: String?, initialPrompt: String?, translate: Boolean, diarize: Boolean): Result<String> =
        runCatching {
            mutex.withLock { WhisperBridge.transcribeWavSegments(wavPath, language, initialPrompt, translate, diarize) }
        }

    override suspend fun release(): Result<Unit> = runCatching {
        mutex.withLock { WhisperBridge.release() }
    }
}
