package com.llamatik.core.platform

/**
 * Progress of a running Stable Diffusion generation, as reported by stable-diffusion.cpp.
 *
 * @property stage Machine-readable stage name (e.g. text encoding, sampling, decoding).
 * @property detail Optional extra detail for the stage.
 * @property unit Unit of [completed] / [total] (e.g. steps, frames).
 * @property samplePass Sampling pass, counted from 1 since the listener was registered.
 * @property total 0 means unknown.
 */
data class SdProgress(
    val stage: String,
    val detail: String?,
    val unit: String?,
    val samplePass: Int,
    val completed: Long,
    val total: Long,
) {
    /** 0f..1f, or null when [total] is unknown. */
    val fraction: Float?
        get() = if (total > 0L) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else null
}

/**
 * Receives progress updates during a generation. It is called synchronously on the thread
 * running the generation, so hand off to your own thread/dispatcher before touching UI.
 */
fun interface SdProgressListener {
    fun onProgress(progress: SdProgress)
}

/** What a ControlNet model was trained to follow. Mirrors stable-diffusion.cpp's `sd_control_type_t`. */
enum class SdControlType(val nativeId: Int) {
    NONE(0),
    OPENPOSE(1),
    DEPTH(2),

    /** Thick lines: scribble, HED, soft edge, TED. */
    SCRIBBLE(3),

    /** Thin lines: canny, lineart, anime lineart, MLSD. */
    CANNY(4),
    NORMAL(5),
    SEGMENT(6),
    TILE(7),

    /** Inpaint, outpaint. */
    REPAINT(8),
}
