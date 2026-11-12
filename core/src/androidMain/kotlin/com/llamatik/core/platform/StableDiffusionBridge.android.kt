package com.llamatik.core.platform

actual object StableDiffusionBridge {
    init {
        // Single native shared library for all JNI bridges (llama/whisper/sd).
        System.loadLibrary("llama_jni")
    }

    actual fun getModelPath(modelFileName: String): String = modelFileName

    actual fun initModel(
        modelPath: String, threads: Int,
        vaePath: String?, t5xxlPath: String?, clipVisionPath: String?,
    ): Boolean = initModelNative(modelPath, threads, vaePath, t5xxlPath, clipVisionPath)

    private external fun initModelNative(
        modelPath: String, threads: Int,
        vaePath: String?, t5xxlPath: String?, clipVisionPath: String?,
    ): Boolean

    actual external fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray

    actual external fun img2img(
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
    ): ByteArray

    actual external fun release()

    actual external fun supportsVideoGeneration(): Boolean

    actual external fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray

    // ---- progress -------------------------------------------------------------------------
    // Kept in a field so the JNI global ref target is not collected while registered.
    private var progressAdapter: NativeProgressAdapter? = null

    actual fun setProgressListener(listener: SdProgressListener?) {
        val adapter = listener?.let { NativeProgressAdapter(it) }
        progressAdapter = adapter
        setProgressCallbackNative(adapter)
    }

    private external fun setProgressCallbackNative(adapter: NativeProgressAdapter?)

    /** Called from JNI (method name/signature are looked up natively — keep in sync). */
    @Suppress("unused")
    internal class NativeProgressAdapter(private val listener: SdProgressListener) {
        fun onNative(stage: String?, detail: String?, unit: String?, samplePass: Int, completed: Long, total: Long) {
            listener.onProgress(SdProgress(stage ?: "", detail, unit, samplePass, completed, total))
        }
    }

    // ---- ControlNet -----------------------------------------------------------------------
    actual external fun loadControlNet(path: String): Boolean
    actual external fun unloadControlNet(): Boolean
    actual external fun hasControlNet(): Boolean

    actual fun generateWithControl(
        controlImageRgba: ByteArray,
        controlImageW: Int,
        controlImageH: Int,
        controlType: SdControlType,
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        controlStrength: Float,
        seed: Long,
    ): ByteArray = generateWithControlNative(
        controlImageRgba, controlImageW, controlImageH, controlType.nativeId,
        prompt, negativePrompt, width, height, steps, cfgScale, controlStrength, seed,
    )

    private external fun generateWithControlNative(
        controlImageRgba: ByteArray,
        controlImageW: Int,
        controlImageH: Int,
        controlType: Int,
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        controlStrength: Float,
        seed: Long,
    ): ByteArray
}
