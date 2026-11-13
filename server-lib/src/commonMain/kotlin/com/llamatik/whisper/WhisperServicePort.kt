package com.llamatik.whisper

interface WhisperServicePort {
    suspend fun initModel(modelPath: String): Result<Boolean>
    suspend fun transcribeWav(wavPath: String, language: String?, initialPrompt: String?): Result<String>
    suspend fun transcribeWavSegments(wavPath: String, language: String?, initialPrompt: String?, translate: Boolean, diarize: Boolean): Result<String>
    suspend fun release(): Result<Unit>
}
