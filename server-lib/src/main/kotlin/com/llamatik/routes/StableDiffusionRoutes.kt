package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.ImageResponse
import com.llamatik.api.Img2ImgRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.api.OkResponse
import com.llamatik.api.StableDiffusionInitRequest
import com.llamatik.api.SupportsVideoResponse
import com.llamatik.api.Txt2ImgRequest
import com.llamatik.api.Txt2VideoRequest
import com.llamatik.api.VideoResponse
import com.llamatik.stablediffusion.StableDiffusionService
import com.llamatik.stablediffusion.StableDiffusionServicePort
import com.llamatik.status.ModelRegistry
import com.llamatik.util.PathValidator
import com.llamatik.util.sanitizedMessage
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.Base64

private const val SD = "$API_VERSION/stablediffusion"
private const val SD_INIT = "$SD/init"
private const val SD_TXT2IMG = "$SD/txt2img"
private const val SD_IMG2IMG = "$SD/img2img"
private const val SD_RELEASE = "$SD/release"
private const val SD_SUPPORTS_VIDEO = "$SD/supportsVideo"
private const val SD_TXT2VIDEO = "$SD/txt2video"

fun Route.stableDiffusionRoutes(service: StableDiffusionServicePort = StableDiffusionService) {
    post(SD_INIT) {
        val req = call.receive<StableDiffusionInitRequest>()
        // Validate all caller-supplied paths. IllegalArgumentException → StatusPages → 400.
        val modelPath = PathValidator.validateModelPath(req.modelPath)
        val vaePath = req.vaePath?.let { PathValidator.validateModelPath(it) }
        val t5xxlPath = req.t5xxlPath?.let { PathValidator.validateModelPath(it) }
        val clipVisionPath = req.clipVisionPath?.let { PathValidator.validateModelPath(it) }
        val result = service.initModel(
            modelPath = modelPath,
            threads = req.threads,
            vaePath = vaePath,
            t5xxlPath = t5xxlPath,
            clipVisionPath = clipVisionPath,
        )
        when {
            result.isSuccess -> {
                val ok = result.getOrThrow()
                if (ok) {
                    val supportsVideo = service.supportsVideoGeneration().getOrElse { false }
                    ModelRegistry.setStableDiffusionLoaded(modelPath, vaePath, t5xxlPath, clipVisionPath, supportsVideo)
                }
                call.respond(InitModelResponse(ok = ok))
            }
            else -> call.respond(HttpStatusCode.BadRequest, result.exceptionOrNull()!!.sanitizedMessage())
        }
    }

    post(SD_TXT2IMG) {
        val req = call.receive<Txt2ImgRequest>()
        service.txt2img(
            prompt = req.prompt,
            negativePrompt = req.negativePrompt,
            width = req.width,
            height = req.height,
            steps = req.steps,
            cfgScale = req.cfgScale,
            seed = req.seed,
        ).fold(
            onSuccess = { bytes ->
                if (bytes.isEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, "Image generation produced no output")
                } else {
                    call.respond(ImageResponse(imageBase64 = Base64.getEncoder().encodeToString(bytes)))
                }
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(SD_IMG2IMG) {
        val req = call.receive<Img2ImgRequest>()
        // Decode before calling into native — an invalid base64 string must return 400,
        // not 500 from an uncaught IllegalArgumentException deep in the stack.
        val initBytes = try {
            Base64.getDecoder().decode(req.initImageBase64)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, "Invalid base64 image data")
            return@post
        }
        service.img2img(
            initImageRgba = initBytes,
            initImageW = req.initImageW,
            initImageH = req.initImageH,
            prompt = req.prompt,
            negativePrompt = req.negativePrompt,
            width = req.width,
            height = req.height,
            steps = req.steps,
            cfgScale = req.cfgScale,
            strength = req.strength,
            seed = req.seed,
        ).fold(
            onSuccess = { bytes ->
                if (bytes.isEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, "Image generation produced no output")
                } else {
                    call.respond(ImageResponse(imageBase64 = Base64.getEncoder().encodeToString(bytes)))
                }
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    post(SD_RELEASE) {
        service.release().fold(
            onSuccess = {
                ModelRegistry.clearStableDiffusion()
                call.respond(OkResponse())
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }

    get(SD_SUPPORTS_VIDEO) {
        service.supportsVideoGeneration().fold(
            onSuccess = { call.respond(SupportsVideoResponse(supportsVideo = it)) },
            onFailure = { call.respond(HttpStatusCode.InternalServerError, it.sanitizedMessage()) }
        )
    }

    post(SD_TXT2VIDEO) {
        val req = call.receive<Txt2VideoRequest>()
        service.txt2video(
            prompt = req.prompt,
            negativePrompt = req.negativePrompt,
            width = req.width,
            height = req.height,
            videoFrames = req.videoFrames,
            fps = req.fps,
            cfgScale = req.cfgScale,
            seed = req.seed,
        ).fold(
            onSuccess = { bytes ->
                if (bytes.isEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, "Video generation produced no output")
                } else {
                    call.respond(VideoResponse(videoBase64 = Base64.getEncoder().encodeToString(bytes)))
                }
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.sanitizedMessage()) }
        )
    }
}
