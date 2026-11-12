package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.ApplyChatTemplateRequest
import com.llamatik.api.ApplyChatTemplateResponse
import com.llamatik.api.GenerateContinueRequest
import com.llamatik.api.GenerateJsonRequest
import com.llamatik.api.GenerateJsonWithContextRequest
import com.llamatik.api.GenerateRequest
import com.llamatik.api.GenerateResponse
import com.llamatik.api.GenerateWithContextRequest
import com.llamatik.api.InitModelRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.api.InitMtpRequest
import com.llamatik.api.ModelChatTemplateResponse
import com.llamatik.api.ModelFinetuneTypeResponse
import com.llamatik.api.OkResponse
import com.llamatik.api.SessionLoadRequest
import com.llamatik.api.SessionResult
import com.llamatik.api.SessionSaveRequest
import com.llamatik.api.UpdateParamsRequest
import com.llamatik.core.platform.GenStream
import com.llamatik.llama.LlamaService
import com.llamatik.llama.LlamaServicePort
import com.llamatik.status.ModelRegistry
import com.llamatik.util.PathValidator
import com.llamatik.util.Sse
import com.llamatik.util.sanitizedMessage
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

private const val GENERATION = "$API_VERSION/generation"
private const val STREAM_DELTA_BUFFER_CAPACITY = 64

private const val GENERATION_INIT = "$GENERATION/init"
private const val GENERATION_GENERATE = "$GENERATION/generate"
private const val GENERATION_GENERATE_WITH_CONTEXT = "$GENERATION/generateWithContext"
private const val GENERATION_GENERATE_JSON = "$GENERATION/generateJson"
private const val GENERATION_GENERATE_JSON_WITH_CONTEXT = "$GENERATION/generateJsonWithContext"
private const val GENERATION_STREAM = "$GENERATION/stream"
private const val GENERATION_STREAM_WITH_CONTEXT = "$GENERATION/streamWithContext"
private const val GENERATION_JSON_STREAM = "$GENERATION/jsonStream"
private const val GENERATION_JSON_STREAM_WITH_CONTEXT = "$GENERATION/jsonStreamWithContext"
private const val GENERATION_PARAMS = "$GENERATION/params"
private const val GENERATION_CANCEL = "$GENERATION/cancel"
private const val GENERATION_SHUTDOWN = "$GENERATION/shutdown"
private const val GENERATION_CONTINUE = "$GENERATION/continue"
private const val GENERATION_SESSION_RESET = "$GENERATION/session/reset"
private const val GENERATION_SESSION_SAVE = "$GENERATION/session/save"
private const val GENERATION_SESSION_LOAD = "$GENERATION/session/load"
private const val GENERATION_CONTINUE_STREAM = "$GENERATION/continueStream"
private const val GENERATION_MTP_INIT = "$GENERATION/mtp/init"
private const val GENERATION_MTP_SHUTDOWN = "$GENERATION/mtp/shutdown"
private const val GENERATION_MODEL_FINETUNE_TYPE = "$GENERATION/model/finetuneType"
private const val GENERATION_MODEL_CHAT_TEMPLATE = "$GENERATION/model/chatTemplate"
private const val GENERATION_MODEL_APPLY_CHAT_TEMPLATE = "$GENERATION/model/applyChatTemplate"

@Suppress("TooGenericExceptionCaught")
fun Route.generationRoutes(service: LlamaServicePort = LlamaService) {

    // --- initialization ---
    post(GENERATION_INIT) {
        val req = call.receive<InitModelRequest>()
        // Throws IllegalArgumentException on invalid path — caught by StatusPages → 400
        val validatedPath = PathValidator.validateModelPath(req.modelPath)
        val result = service.initGenerateModel(validatedPath)
        when {
            result.isSuccess -> {
                val ok = result.getOrThrow()
                if (ok) {
                    // Fetch capabilities while the model is freshly loaded.
                    // These are suspend calls — fine here as we're inside a Ktor coroutine.
                    val finetuneType = service.getModelFinetuneType().getOrNull()
                    val hasChatTemplate = service.getModelChatTemplate().getOrNull() != null
                    ModelRegistry.setGenerationLoaded(validatedPath, finetuneType, hasChatTemplate)
                }
                call.respond(InitModelResponse(ok = ok))
            }
            else -> call.respond(HttpStatusCode.BadRequest, result.exceptionOrNull()!!.sanitizedMessage())
        }
    }

    // --- non-streaming ---
    post(GENERATION_GENERATE) {
        val req = call.receive<GenerateRequest>()
        service.generate(req.prompt).fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_GENERATE_WITH_CONTEXT) {
        val req = call.receive<GenerateWithContextRequest>()
        service.generateWithContext(req.systemPrompt, req.contextBlock, req.userPrompt).fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_GENERATE_JSON) {
        val req = call.receive<GenerateJsonRequest>()
        service.generateJson(req.prompt, req.jsonSchema).fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_GENERATE_JSON_WITH_CONTEXT) {
        val req = call.receive<GenerateJsonWithContextRequest>()
        service.generateJsonWithContext(req.systemPrompt, req.contextBlock, req.userPrompt, req.jsonSchema).fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_CONTINUE) {
        val req = call.receive<GenerateContinueRequest>()
        service.generateContinue(req.prompt).fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    // --- params / cancel / shutdown ---
    post(GENERATION_PARAMS) {
        val req = call.receive<UpdateParamsRequest>()
        service.updateGenerateParams(
            req.temperature, req.maxTokens, req.topP, req.topK, req.repeatPenalty,
            req.contextLength, req.numThreads, req.useMmap, req.flashAttention, req.batchSize,
            req.gpuLayers
        ).fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_CANCEL) {
        service.cancelGenerate().fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_SHUTDOWN) {
        service.shutdown().fold(
            onSuccess = {
                ModelRegistry.clearGeneration()
                call.respond(OkResponse())
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    // --- KV session management ---
    post(GENERATION_SESSION_RESET) {
        service.sessionReset().fold(
            onSuccess = { call.respond(SessionResult(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_SESSION_SAVE) {
        val req = call.receive<SessionSaveRequest>()
        // Session paths are validated against SESSIONS_DIR to prevent arbitrary file writes.
        service.sessionSave(PathValidator.validateSessionPath(req.path)).fold(
            onSuccess = { call.respond(SessionResult(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_SESSION_LOAD) {
        val req = call.receive<SessionLoadRequest>()
        service.sessionLoad(PathValidator.validateSessionPath(req.path)).fold(
            onSuccess = { call.respond(SessionResult(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    // --- model metadata ---
    get(GENERATION_MODEL_FINETUNE_TYPE) {
        service.getModelFinetuneType().fold(
            onSuccess = { call.respond(ModelFinetuneTypeResponse(finetuneType = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    get(GENERATION_MODEL_CHAT_TEMPLATE) {
        service.getModelChatTemplate().fold(
            onSuccess = { call.respond(ModelChatTemplateResponse(chatTemplate = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_MODEL_APPLY_CHAT_TEMPLATE) {
        val req = call.receive<ApplyChatTemplateRequest>()
        val pairs = req.messages.map { it.role to it.content }
        service.applyChatTemplate(pairs, req.addAssistantPrefix).fold(
            onSuccess = { call.respond(ApplyChatTemplateResponse(prompt = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    // --- MTP (speculative decoding) ---
    post(GENERATION_MTP_INIT) {
        val req = call.receive<InitMtpRequest>()
        val validatedPath = PathValidator.validateModelPath(req.modelPath)
        service.initMtp(validatedPath, req.draftLen).fold(
            onSuccess = {
                if (it) ModelRegistry.setMtpLoaded(validatedPath)
                call.respond(InitModelResponse(ok = it))
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(GENERATION_MTP_SHUTDOWN) {
        service.shutdownMtp().fold(
            onSuccess = {
                ModelRegistry.clearMtp()
                call.respond(OkResponse())
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    // --- streaming (SSE) ---
    postSseStream<GenerateRequest>(GENERATION_STREAM) { req, cb ->
        service.generateStream(req.prompt, cb)
    }

    postSseStream<GenerateWithContextRequest>(GENERATION_STREAM_WITH_CONTEXT) { req, cb ->
        service.generateStreamWithContext(req.systemPrompt, req.contextBlock, req.userPrompt, cb)
    }

    postSseStream<GenerateJsonRequest>(GENERATION_JSON_STREAM) { req, cb ->
        service.generateJsonStream(req.prompt, req.jsonSchema, cb)
    }

    postSseStream<GenerateJsonWithContextRequest>(GENERATION_JSON_STREAM_WITH_CONTEXT) { req, cb ->
        service.generateJsonStreamWithContext(req.systemPrompt, req.contextBlock, req.userPrompt, req.jsonSchema, cb)
    }

    postSseStream<GenerateContinueRequest>(GENERATION_CONTINUE_STREAM) { req, cb ->
        service.generateContinueStream(req.prompt, cb)
    }
}

@Suppress("TooGenericExceptionCaught")
private inline fun <reified T : Any> Route.postSseStream(
    path: String,
    crossinline stream: (T, GenStream) -> Unit
) {
    post(path) {
        val req = call.receive<T>()

        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            val deltas = Channel<String>(capacity = STREAM_DELTA_BUFFER_CAPACITY)
            val done = Channel<Unit>(capacity = 1)
            val errors = Channel<String>(capacity = 1)
            val cb = Sse.genStreamCallback(
                onDelta = { deltas.trySendBlocking(it).isSuccess },
                onDone = { done.trySend(Unit).isSuccess },
                onError = { errors.trySend(it).isSuccess }
            )

            val streamJob = CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO) {
                try {
                    stream(req, cb)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    errors.trySend(e.message ?: "Streaming failed")
                }
            }

            try {
                Sse.pipe(writer = this, deltas = deltas, done = done, errors = errors)
            } finally {
                streamJob.cancel()
            }
        }
    }
}
