package com.llamatik.network.client

import co.touchlab.kermit.Logger
import com.llamatik.network.client.dto.AnalyzeImageRequest
import com.llamatik.network.client.dto.ApplyChatTemplateRequest
import com.llamatik.network.client.dto.ApplyChatTemplateResponse
import com.llamatik.network.client.dto.ChatMessageDto
import com.llamatik.network.client.dto.EmbedRequest
import com.llamatik.network.client.dto.EmbedResponse
import com.llamatik.network.client.dto.GenerateContinueRequest
import com.llamatik.network.client.dto.GenerateJsonRequest
import com.llamatik.network.client.dto.GenerateJsonWithContextRequest
import com.llamatik.network.client.dto.GenerateRequest
import com.llamatik.network.client.dto.GenerateResponse
import com.llamatik.network.client.dto.GenerateWithContextRequest
import com.llamatik.network.client.dto.ImageResponse
import com.llamatik.network.client.dto.InitModelRequest
import com.llamatik.network.client.dto.InitModelResponse
import com.llamatik.network.client.dto.InitMtpRequest
import com.llamatik.network.client.dto.ModelChatTemplateResponse
import com.llamatik.network.client.dto.ModelFinetuneTypeResponse
import com.llamatik.network.client.dto.ModelsStatusResponse
import com.llamatik.network.client.dto.MultimodalInitRequest
import com.llamatik.network.client.dto.OkResponse
import com.llamatik.network.client.dto.PairingRequestBody
import com.llamatik.network.client.dto.PairingRequestResponse
import com.llamatik.network.client.dto.PairingStatusResponse
import com.llamatik.network.client.dto.SessionResult
import com.llamatik.network.client.dto.SseEventDto
import com.llamatik.network.client.dto.StableDiffusionInitRequest
import com.llamatik.network.client.dto.SupportsVideoResponse
import com.llamatik.network.client.dto.TranscribeRequest
import com.llamatik.network.client.dto.TranscribeResponse
import com.llamatik.network.client.dto.TranscribeSegmentsRequest
import com.llamatik.network.client.dto.TranscribeSegmentsResponse
import com.llamatik.network.client.dto.Txt2ImgRequest
import com.llamatik.network.client.dto.UpdateParamsRequest
import com.llamatik.network.client.dto.WhisperInitRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.random.Random

class LlamatikServerClient(
    val config: LlamatikServerConfig,
    private val httpClient: HttpClient = defaultHttpClient(config.accessToken),
    private val retryPolicy: StreamRetryPolicy = StreamRetryPolicy(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    // --- Generation ---

    suspend fun initModel(modelPath: String): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/init") {
            setBody(InitModelRequest(modelPath))
        }.body<InitModelResponse>().ok
    }.getOrElse { e -> Logger.e(e) { "initModel failed" }; false }

    suspend fun generate(prompt: String): String =
        httpClient.post("${config.baseUrl}/generation/generate") {
            setBody(GenerateRequest(prompt))
        }.body<GenerateResponse>().text

    fun streamGenerate(prompt: String): Flow<String> =
        resumableSse("/generation/stream", GenerateRequest(prompt))

    fun streamGenerateWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
    ): Flow<String> =
        resumableSse("/generation/streamWithContext", GenerateWithContextRequest(systemPrompt, contextBlock, userPrompt))

    fun streamContinue(prompt: String): Flow<String> =
        resumableSse("/generation/continueStream", GenerateContinueRequest(prompt))

    fun streamGenerateJson(prompt: String, jsonSchema: String? = null): Flow<String> =
        resumableSse("/generation/jsonStream", GenerateJsonRequest(prompt, jsonSchema))

    fun streamGenerateJsonWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
        jsonSchema: String? = null,
    ): Flow<String> =
        resumableSse("/generation/jsonStreamWithContext", GenerateJsonWithContextRequest(systemPrompt, contextBlock, userPrompt, jsonSchema))

    suspend fun updateParams(params: UpdateParamsRequest): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/params") {
            setBody(params)
        }.body<OkResponse>().ok
    }.getOrElse { false }

    suspend fun cancelGeneration(): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/cancel").body<OkResponse>().ok
    }.getOrElse { false }

    suspend fun shutdownModel(): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/shutdown").body<OkResponse>().ok
    }.getOrElse { false }

    suspend fun resetSession(): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/session/reset").body<SessionResult>().ok
    }.getOrElse { false }

    suspend fun initMtp(modelPath: String, draftLen: Int = 3): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/mtp/init") {
            setBody(InitMtpRequest(modelPath, draftLen))
        }.body<InitModelResponse>().ok
    }.getOrElse { false }

    suspend fun shutdownMtp(): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/generation/mtp/shutdown").body<OkResponse>().ok
    }.getOrElse { false }

    suspend fun getFinetuneType(): String? = runCatching {
        httpClient.get("${config.baseUrl}/generation/model/finetuneType")
            .body<ModelFinetuneTypeResponse>().finetuneType
    }.getOrNull()

    suspend fun getChatTemplate(): String? = runCatching {
        httpClient.get("${config.baseUrl}/generation/model/chatTemplate")
            .body<ModelChatTemplateResponse>().chatTemplate
    }.getOrNull()

    suspend fun applyChatTemplate(
        messages: List<ChatMessageDto>,
        addAssistantPrefix: Boolean = true,
    ): String? = runCatching {
        httpClient.post("${config.baseUrl}/generation/model/applyChatTemplate") {
            setBody(ApplyChatTemplateRequest(messages, addAssistantPrefix))
        }.body<ApplyChatTemplateResponse>().prompt
    }.getOrNull()

    // --- Embeddings ---

    suspend fun initEmbedModel(modelPath: String): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/embeddings/init") {
            setBody(InitModelRequest(modelPath))
        }.body<InitModelResponse>().ok
    }.getOrElse { false }

    suspend fun embed(input: String): List<Float> =
        httpClient.post("${config.baseUrl}/embeddings/embed") {
            setBody(EmbedRequest(input))
        }.body<EmbedResponse>().embedding

    // --- Multimodal ---

    suspend fun initMultimodal(modelPath: String, mmprojPath: String): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/multimodal/init") {
            setBody(MultimodalInitRequest(modelPath, mmprojPath))
        }.body<InitModelResponse>().ok
    }.getOrElse { false }

    fun streamAnalyzeImage(imageBase64: String, prompt: String): Flow<String> =
        resumableSse("/multimodal/analyzeStream", AnalyzeImageRequest(imageBase64, prompt))

    suspend fun cancelMultimodal(): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/multimodal/cancel").body<OkResponse>().ok
    }.getOrElse { false }

    // --- Whisper ---

    suspend fun initWhisper(modelPath: String): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/whisper/init") {
            setBody(WhisperInitRequest(modelPath))
        }.body<InitModelResponse>().ok
    }.getOrElse { false }

    suspend fun transcribe(
        wavPath: String,
        language: String? = null,
        initialPrompt: String? = null,
    ): String = httpClient.post("${config.baseUrl}/whisper/transcribe") {
        setBody(TranscribeRequest(wavPath, language, initialPrompt))
    }.body<TranscribeResponse>().text

    suspend fun transcribeSegments(
        wavPath: String,
        language: String? = null,
        initialPrompt: String? = null,
        translate: Boolean = false,
        diarize: Boolean = false,
    ): String = httpClient.post("${config.baseUrl}/whisper/transcribeSegments") {
        setBody(TranscribeSegmentsRequest(wavPath, language, initialPrompt, translate, diarize))
    }.body<TranscribeSegmentsResponse>().json

    // --- Stable Diffusion ---

    suspend fun initStableDiffusion(modelPath: String, threads: Int = -1): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/stablediffusion/init") {
            setBody(StableDiffusionInitRequest(modelPath, threads))
        }.body<InitModelResponse>().ok
    }.getOrElse { false }

    suspend fun txt2img(request: Txt2ImgRequest): String =
        httpClient.post("${config.baseUrl}/stablediffusion/txt2img") {
            setBody(request)
        }.body<ImageResponse>().imageBase64

    suspend fun releaseStableDiffusion(): Boolean = runCatching {
        httpClient.post("${config.baseUrl}/stablediffusion/release").body<OkResponse>().ok
    }.getOrElse { false }

    // --- Health & Capabilities ---

    suspend fun isReachable(): Boolean = runCatching {
        httpClient.get("http://${config.host}:${config.port}/").status.isSuccess()
    }.getOrElse { false }

    /**
     * Returns a snapshot of every inference bridge from `GET /v1/models`.
     * Null if the server is unreachable or returns an error.
     */
    suspend fun getModelsStatus(): ModelsStatusResponse? = runCatching {
        httpClient.get("${config.baseUrl}/models").body<ModelsStatusResponse>()
    }.getOrNull()

    /** Whether this client is allowed to use the server, needs approval, or can't reach it. */
    enum class AccessProbe { AUTHORIZED, PAIRING_REQUIRED, UNREACHABLE }

    suspend fun probeAccess(): AccessProbe = runCatching {
        when (httpClient.get("${config.baseUrl}/models").status) {
            HttpStatusCode.Unauthorized -> AccessProbe.PAIRING_REQUIRED
            HttpStatusCode.OK -> AccessProbe.AUTHORIZED
            else -> AccessProbe.UNREACHABLE
        }
    }.getOrElse { AccessProbe.UNREACHABLE }

    /**
     * Asks the server's owner to approve this device. Returns the request id to poll with
     * [pollPairing], or null when the server refused (e.g. too many pending requests).
     */
    suspend fun requestPairing(clientId: String, clientName: String): String? = runCatching {
        val response = httpClient.post("${config.baseUrl}/pairing/request") {
            setBody(PairingRequestBody(clientId, clientName))
        }
        if (response.status.isSuccess()) response.body<PairingRequestResponse>().requestId else null
    }.getOrNull()

    /** Result of polling a pairing request. [token] is non-null once, when approved. */
    suspend fun pollPairing(requestId: String): PairingStatusResponse? = runCatching {
        httpClient.get("${config.baseUrl}/pairing/$requestId").body<PairingStatusResponse>()
    }.getOrNull()

    /** Convenience shortcut: check whether the loaded SD model supports video. */
    suspend fun supportsVideoGeneration(): Boolean = runCatching {
        httpClient.get("${config.baseUrl}/stablediffusion/supportsVideo")
            .body<SupportsVideoResponse>().supportsVideo
    }.getOrElse { false }

    // --- SSE parser ---

    /**
     * Streams a server-sent-events endpoint and transparently survives connection drops.
     *
     * Every stream gets a client-chosen id. When the connection fails or stalls, the request is
     * re-sent with the same id and `Last-Event-ID` so the server replays what was missed and
     * continues the still-running generation, with no duplicated or lost text. Up to
     * [StreamRetryPolicy.maxAttempts] consecutive failures are tolerated (the counter resets
     * whenever progress is made).
     *
     * Throws [StreamExpiredException] when the server can no longer resume (restarted, session
     * expired, or an older server without resume support): the caller may restart the request,
     * discarding the partial text. Throws the last connection error when retries are exhausted.
     */
    private inline fun <reified T : Any> resumableSse(path: String, body: T): Flow<String> = channelFlow {
        val streamId = newStreamId()
        var received = 0
        var failures = 0
        while (true) {
            val receivedBefore = received
            try {
                httpClient.preparePost("${config.baseUrl}$path") {
                    header(STREAM_ID_HEADER, streamId)
                    if (received > 0) header(LAST_EVENT_ID_HEADER, received.toString())
                    setBody(body)
                }.execute { response ->
                    if (response.status == HttpStatusCode.Gone) throw StreamExpiredException("Stream expired on server")
                    if (!response.status.isSuccess()) {
                        throw RemoteInferenceException("Server responded ${response.status.value}")
                    }
                    // A server without resume support would restart the generation from scratch.
                    if (received > 0 && response.headers[STREAM_RESUME_HEADER] != "1") {
                        throw StreamExpiredException("Server does not support resuming streams")
                    }
                    parseSse(response.bodyAsChannel()) { received = it }
                }
                return@channelFlow
            } catch (e: CancellationException) {
                throw e
            } catch (e: RemoteInferenceException) {
                throw e
            } catch (e: StreamExpiredException) {
                throw e
            } catch (e: Exception) {
                if (received > receivedBefore) failures = 0
                failures++
                if (failures > retryPolicy.maxAttempts) throw e
                Logger.w(e) { "Stream $path interrupted after $received events (retry $failures/${retryPolicy.maxAttempts})" }
                delay(retryPolicy.delayFor(failures))
            }
        }
    }

    /**
     * Parses an SSE stream, reporting the id of each processed event via [onEventId]. Throws
     * [StreamInterruptedException] when the connection ends or stalls before the server sent its
     * `done` event, so a truncated response is never mistaken for a complete one.
     */
    private suspend fun ProducerScope<String>.parseSse(
        byteChannel: ByteReadChannel,
        onEventId: (Int) -> Unit,
    ) {
        var finished = false
        var pendingId: Int? = null
        try {
            while (isActive) {
                // Wrapped in a list so a null line (EOF) is distinguishable from a timeout.
                val read = withTimeoutOrNull(retryPolicy.idleTimeoutMs) { listOf(byteChannel.readUTF8Line()) }
                    ?: throw StreamInterruptedException("Stream stalled")
                val line = read[0] ?: break
                if (line.startsWith("id:")) {
                    pendingId = line.removePrefix("id:").trim().toIntOrNull()
                } else if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data.isEmpty()) continue
                    try {
                        val event = json.decodeFromString<SseEventDto>(data)
                        // Record progress before emitting so a drop right after never duplicates text.
                        pendingId?.let(onEventId)
                        when (event.event) {
                            "delta" -> event.text?.let { send(it) }
                            "done" -> finished = true
                            "error" -> throw RemoteInferenceException(event.message ?: "Remote inference error")
                        }
                        if (finished) break
                    } catch (e: RemoteInferenceException) {
                        throw e
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // skip malformed SSE line
                    }
                    pendingId = null
                }
            }
        } finally {
            byteChannel.cancel(null)
        }
        if (!finished && isActive) throw StreamInterruptedException("Connection closed before generation finished")
    }

    private fun newStreamId(): String =
        Random.nextLong().toULong().toString(16) + Random.nextLong().toULong().toString(16)

    companion object {
        fun defaultHttpClient(accessToken: String? = null): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            // Without a default Content-Type, ktor cannot pick a serializer for setBody(...)
            defaultRequest {
                contentType(ContentType.Application.Json)
                if (!accessToken.isNullOrBlank()) header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }
}

/** Reconnect behaviour for dropped streams. */
data class StreamRetryPolicy(
    /** Consecutive failed reconnects tolerated before giving up. Resets after any progress. */
    val maxAttempts: Int = 4,
    val baseDelayMs: Long = 1_000L,
    val maxDelayMs: Long = 8_000L,
    /** A stream with no data for this long is considered dead (e.g. silent network switch). */
    val idleTimeoutMs: Long = 120_000L,
) {
    internal fun delayFor(failure: Int): Long =
        (baseDelayMs shl (failure - 1).coerceIn(0, 10)).coerceAtMost(maxDelayMs)
}

/** The SSE connection dropped or stalled before the server reported completion. */
class StreamInterruptedException(message: String) : Exception(message)

/** The server cannot resume this stream; the request has to be restarted from scratch. */
class StreamExpiredException(message: String) : Exception(message)

/** The server reported a failure for this request. Not retried: it would fail again. */
class RemoteInferenceException(message: String) : Exception(message)

internal const val STREAM_ID_HEADER = "X-Stream-Id"
internal const val LAST_EVENT_ID_HEADER = "Last-Event-ID"
internal const val STREAM_RESUME_HEADER = "X-Stream-Resume"
