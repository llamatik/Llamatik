package com.llamatik.fakes

import com.llamatik.core.platform.GenStream
import com.llamatik.llama.LlamaServicePort

class FakeLlamaService(
    private val initEmbedResult: Result<Boolean> = Result.success(true),
    private val initGenerateResult: Result<Boolean> = Result.success(true),
    private val embedResult: Result<FloatArray> = Result.success(floatArrayOf(0.1f, 0.2f, 0.3f)),
    private val generateResult: Result<String> = Result.success("generated text"),
    private val generateWithContextResult: Result<String> = Result.success("context text"),
    private val generateJsonResult: Result<String> = Result.success("{\"key\":\"value\"}"),
    private val generateJsonWithContextResult: Result<String> = Result.success("{\"key\":\"ctx\"}"),
    private val generateContinueResult: Result<String> = Result.success("continued text"),
    private val updateParamsResult: Result<Unit> = Result.success(Unit),
    private val cancelResult: Result<Unit> = Result.success(Unit),
    private val shutdownResult: Result<Unit> = Result.success(Unit),
    private val sessionResetResult: Result<Boolean> = Result.success(true),
    private val sessionSaveResult: Result<Boolean> = Result.success(true),
    private val sessionLoadResult: Result<Boolean> = Result.success(true),
    private val finetuneTypeResult: Result<String?> = Result.success("instruct"),
    private val chatTemplateResult: Result<String?> = Result.success("{{messages}}"),
    private val applyChatTemplateResult: Result<String?> = Result.success("<|user|>hi"),
    private val streamDeltas: List<String> = listOf("hello", " world"),
    private val initMtpResult: Result<Boolean> = Result.success(true),
    private val shutdownMtpResult: Result<Unit> = Result.success(Unit),
) : LlamaServicePort {

    override suspend fun initModel(modelPath: String) = initEmbedResult
    override suspend fun embed(input: String) = embedResult
    override suspend fun initGenerateModel(modelPath: String) = initGenerateResult
    override suspend fun generate(prompt: String) = generateResult
    override suspend fun generateWithContext(systemPrompt: String, contextBlock: String, userPrompt: String) = generateWithContextResult
    override suspend fun generateJson(prompt: String, jsonSchema: String?) = generateJsonResult
    override suspend fun generateJsonWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, jsonSchema: String?) = generateJsonWithContextResult
    override suspend fun generateContinue(prompt: String) = generateContinueResult
    override suspend fun updateGenerateParams(temperature: Float, maxTokens: Int, topP: Float, topK: Int, repeatPenalty: Float, contextLength: Int, numThreads: Int, useMmap: Boolean, flashAttention: Boolean, batchSize: Int, gpuLayers: Int) = updateParamsResult
    override suspend fun cancelGenerate() = cancelResult
    override suspend fun shutdown() = shutdownResult
    override suspend fun sessionReset() = sessionResetResult
    override suspend fun sessionSave(path: String) = sessionSaveResult
    override suspend fun sessionLoad(path: String) = sessionLoadResult
    override suspend fun getModelFinetuneType() = finetuneTypeResult
    override suspend fun getModelChatTemplate() = chatTemplateResult
    override suspend fun applyChatTemplate(messages: List<Pair<String, String>>, addAssistantPrefix: Boolean) = applyChatTemplateResult

    override fun generateStream(prompt: String, callback: GenStream) {
        streamDeltas.forEach { callback.onDelta(it) }
        callback.onComplete()
    }

    override fun generateStreamWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, callback: GenStream) {
        streamDeltas.forEach { callback.onDelta(it) }
        callback.onComplete()
    }

    override fun generateJsonStream(prompt: String, jsonSchema: String?, callback: GenStream) {
        streamDeltas.forEach { callback.onDelta(it) }
        callback.onComplete()
    }

    override fun generateJsonStreamWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, jsonSchema: String?, callback: GenStream) {
        streamDeltas.forEach { callback.onDelta(it) }
        callback.onComplete()
    }

    override fun generateContinueStream(prompt: String, callback: GenStream) {
        streamDeltas.forEach { callback.onDelta(it) }
        callback.onComplete()
    }

    override suspend fun initMtp(modelPath: String, draftLen: Int): Result<Boolean> = initMtpResult
    override suspend fun shutdownMtp(): Result<Unit> = shutdownMtpResult
}
