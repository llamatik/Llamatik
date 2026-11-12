package com.llamatik.llama

import com.llamatik.core.platform.GenStream

interface LlamaServicePort {
    suspend fun initModel(modelPath: String): Result<Boolean>
    suspend fun embed(input: String): Result<FloatArray>
    suspend fun initGenerateModel(modelPath: String): Result<Boolean>
    suspend fun generate(prompt: String): Result<String>
    suspend fun generateWithContext(systemPrompt: String, contextBlock: String, userPrompt: String): Result<String>
    suspend fun generateJson(prompt: String, jsonSchema: String?): Result<String>
    suspend fun generateJsonWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, jsonSchema: String?): Result<String>
    suspend fun generateContinue(prompt: String): Result<String>
    suspend fun updateGenerateParams(temperature: Float, maxTokens: Int, topP: Float, topK: Int, repeatPenalty: Float, contextLength: Int, numThreads: Int, useMmap: Boolean, flashAttention: Boolean, batchSize: Int, gpuLayers: Int = 0): Result<Unit>
    suspend fun cancelGenerate(): Result<Unit>
    suspend fun shutdown(): Result<Unit>
    suspend fun sessionReset(): Result<Boolean>
    suspend fun sessionSave(path: String): Result<Boolean>
    suspend fun sessionLoad(path: String): Result<Boolean>
    suspend fun getModelFinetuneType(): Result<String?>
    suspend fun getModelChatTemplate(): Result<String?>
    suspend fun applyChatTemplate(messages: List<Pair<String, String>>, addAssistantPrefix: Boolean): Result<String?>
    fun generateStream(prompt: String, callback: GenStream)
    fun generateStreamWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, callback: GenStream)
    fun generateJsonStream(prompt: String, jsonSchema: String?, callback: GenStream)
    fun generateJsonStreamWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, jsonSchema: String?, callback: GenStream)
    fun generateContinueStream(prompt: String, callback: GenStream)
    suspend fun initMtp(modelPath: String, draftLen: Int = 3): Result<Boolean>
    suspend fun shutdownMtp(): Result<Unit>
}
