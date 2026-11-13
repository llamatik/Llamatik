package com.llamatik.fakes

import com.llamatik.stablediffusion.StableDiffusionServicePort

class FakeStableDiffusionService(
    private val initResult: Result<Boolean> = Result.success(true),
    private val txt2imgResult: Result<ByteArray> = Result.success(ByteArray(512 * 512 * 4) { 0xFF.toByte() }),
    private val img2imgResult: Result<ByteArray> = Result.success(ByteArray(512 * 512 * 4) { 0xAB.toByte() }),
    private val releaseResult: Result<Unit> = Result.success(Unit),
    private val supportsVideoResult: Result<Boolean> = Result.success(true),
    private val txt2videoResult: Result<ByteArray> = Result.success(buildFakeVideoPacket()),
) : StableDiffusionServicePort {

    override suspend fun initModel(
        modelPath: String,
        threads: Int,
        vaePath: String?,
        t5xxlPath: String?,
        clipVisionPath: String?,
    ) = initResult

    override suspend fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ) = txt2imgResult

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
    ) = img2imgResult

    override suspend fun release() = releaseResult

    override suspend fun supportsVideoGeneration() = supportsVideoResult

    override suspend fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ) = txt2videoResult
}

/** Builds a minimal valid packed video buffer: [frameCount | width | height | fps | RGBA…]. */
fun buildFakeVideoPacket(frameCount: Int = 4, width: Int = 8, height: Int = 8, fps: Int = 8): ByteArray {
    fun Int.toLeBytes() = byteArrayOf(
        (this and 0xFF).toByte(),
        ((this shr 8) and 0xFF).toByte(),
        ((this shr 16) and 0xFF).toByte(),
        ((this shr 24) and 0xFF).toByte(),
    )
    val header = frameCount.toLeBytes() + width.toLeBytes() + height.toLeBytes() + fps.toLeBytes()
    val frames = ByteArray(frameCount * width * height * 4) { 0xAB.toByte() }
    return header + frames
}
