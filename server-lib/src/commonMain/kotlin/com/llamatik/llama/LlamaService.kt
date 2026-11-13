package com.llamatik.llama

import com.llamatik.core.platform.GenStream
import com.llamatik.core.platform.LlamaBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * JVM-side façade over the KMP [LlamaBridge] for use in the Ktor backend.
 *
 * The llama.cpp JNI integration is typically not re-entrant. This service
 * serializes calls that mutate or use the current generation session.
 */
object LlamaService : LlamaServicePort {

    private val mutex = Mutex()

    override suspend fun initModel(modelPath: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.initEmbedModel(modelPath) }
    }

    override suspend fun embed(input: String): Result<FloatArray> = runCatching {
        mutex.withLock { LlamaBridge.embed(input) }
    }

    override suspend fun initGenerateModel(modelPath: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.initGenerateModel(modelPath) }
    }

    override suspend fun generate(prompt: String): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generate(prompt) }
    }

    override suspend fun generateWithContext(systemPrompt: String, contextBlock: String, userPrompt: String): Result<String> =
        runCatching {
            mutex.withLock { LlamaBridge.generateWithContext(systemPrompt, contextBlock, userPrompt) }
        }

    override suspend fun generateJson(prompt: String, jsonSchema: String?): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generateJson(prompt, jsonSchema) }
    }

    override suspend fun generateJsonWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
        jsonSchema: String?
    ): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generateJsonWithContext(systemPrompt, contextBlock, userPrompt, jsonSchema) }
    }

    override suspend fun updateGenerateParams(
        temperature: Float,
        maxTokens: Int,
        topP: Float,
        topK: Int,
        repeatPenalty: Float,
        contextLength: Int,
        numThreads: Int,
        useMmap: Boolean,
        flashAttention: Boolean,
        batchSize: Int,
        gpuLayers: Int,
    ): Result<Unit> = runCatching {
        mutex.withLock {
            LlamaBridge.updateGenerateParams(
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                topK = topK,
                repeatPenalty = repeatPenalty,
                contextLength = contextLength,
                numThreads = numThreads,
                useMmap = useMmap,
                flashAttention = flashAttention,
                batchSize = batchSize,
                gpuLayers = gpuLayers,
            )
        }
    }

    override suspend fun generateContinue(prompt: String): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generateContinue(prompt) }
    }

    override suspend fun sessionReset(): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.sessionReset() }
    }

    override suspend fun sessionSave(path: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.sessionSave(path) }
    }

    override suspend fun sessionLoad(path: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.sessionLoad(path) }
    }

    override suspend fun getModelFinetuneType(): Result<String?> = runCatching {
        mutex.withLock { LlamaBridge.getModelFinetuneType() }
    }

    override suspend fun getModelChatTemplate(): Result<String?> = runCatching {
        mutex.withLock { LlamaBridge.getModelChatTemplate() }
    }

    override suspend fun applyChatTemplate(messages: List<Pair<String, String>>, addAssistantPrefix: Boolean): Result<String?> =
        runCatching {
            mutex.withLock { LlamaBridge.applyChatTemplate(messages, addAssistantPrefix) }
        }

    override fun generateStream(prompt: String, callback: GenStream) {
        LlamaBridge.generateStream(prompt, callback)
    }

    override fun generateStreamWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, callback: GenStream) {
        LlamaBridge.generateStreamWithContext(systemPrompt, contextBlock, userPrompt, callback)
    }

    override fun generateJsonStream(prompt: String, jsonSchema: String?, callback: GenStream) {
        LlamaBridge.generateJsonStream(prompt, jsonSchema, callback)
    }

    override fun generateJsonStreamWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
        jsonSchema: String?,
        callback: GenStream
    ) {
        LlamaBridge.generateJsonStreamWithContext(systemPrompt, contextBlock, userPrompt, jsonSchema, callback)
    }

    override suspend fun cancelGenerate(): Result<Unit> = runCatching {
        mutex.withLock { LlamaBridge.nativeCancelGenerate() }
    }

    override suspend fun shutdown(): Result<Unit> = runCatching {
        mutex.withLock { LlamaBridge.shutdown() }
    }

    override fun generateContinueStream(prompt: String, callback: GenStream) {
        LlamaBridge.generateContinueStream(prompt, callback)
    }

    override suspend fun initMtp(modelPath: String, draftLen: Int): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.initMtp(modelPath, draftLen) }
    }

    override suspend fun shutdownMtp(): Result<Unit> = runCatching {
        mutex.withLock { LlamaBridge.shutdownMtp() }
    }
}
