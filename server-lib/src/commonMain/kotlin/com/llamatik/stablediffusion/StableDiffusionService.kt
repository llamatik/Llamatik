package com.llamatik.stablediffusion

import com.llamatik.core.platform.StableDiffusionBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object StableDiffusionService : StableDiffusionServicePort {

    private val mutex = Mutex()

    override suspend fun initModel(
        modelPath: String,
        threads: Int,
        vaePath: String?,
        t5xxlPath: String?,
        clipVisionPath: String?,
    ): Result<Boolean> = runCatching {
        mutex.withLock {
            StableDiffusionBridge.initModel(
                modelPath = modelPath,
                threads = threads,
                vaePath = vaePath,
                t5xxlPath = t5xxlPath,
                clipVisionPath = clipVisionPath,
            )
        }
    }

    override suspend fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ): Result<ByteArray> = runCatching {
        mutex.withLock {
            StableDiffusionBridge.txt2img(
                prompt = prompt,
                negativePrompt = negativePrompt,
                width = width,
                height = height,
                steps = steps,
                cfgScale = cfgScale,
                seed = seed,
            )
        }
    }

    override suspend fun img2img(
        initImageRgba: ByteArray,
        initImageW: Int,
        initImageH: Int,
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        strength: Float,
        seed: Long,
    ): Result<ByteArray> = runCatching {
        mutex.withLock {
            StableDiffusionBridge.img2img(
                initImageRgba = initImageRgba,
                initImageW = initImageW,
                initImageH = initImageH,
                prompt = prompt,
                negativePrompt = negativePrompt,
                width = width,
                height = height,
                steps = steps,
                cfgScale = cfgScale,
                strength = strength,
                seed = seed,
            )
        }
    }

    override suspend fun release(): Result<Unit> = runCatching {
        mutex.withLock { StableDiffusionBridge.release() }
    }

    override suspend fun supportsVideoGeneration(): Result<Boolean> = runCatching {
        mutex.withLock { StableDiffusionBridge.supportsVideoGeneration() }
    }

    override suspend fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ): Result<ByteArray> = runCatching {
        mutex.withLock {
            StableDiffusionBridge.txt2video(
                prompt = prompt,
                negativePrompt = negativePrompt,
                width = width,
                height = height,
                videoFrames = videoFrames,
                fps = fps,
                cfgScale = cfgScale,
                seed = seed,
            )
        }
    }
}
