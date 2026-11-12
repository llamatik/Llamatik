package com.llamatik.core.platform

import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

actual object StableDiffusionBridge {
    init {
        // Reuse the same JNI shared library that contains llama/whisper/sd exports.
        loadNativeFromResources()
    }

    /** Forces the shared native library to be extracted and loaded (used by [VideoUpscalerBridge]). */
    internal fun ensureNativeLoaded() {}

    actual fun getModelPath(modelFileName: String): String {
        // Desktop: callers typically pass an absolute path to downloaded model files.
        val p = Path(modelFileName)
        return if (Files.exists(p)) p.toAbsolutePath().toString() else modelFileName
    }

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

    private fun loadNativeFromResources() {
        val os = System.getProperty("os.name").lowercase()
        val platform = when {
            os.contains("mac") -> "macos"
            os.contains("linux") -> "linux"
            os.contains("win") -> "windows"
            else -> error("Unsupported OS: $os")
        }

        val libFileName = System.mapLibraryName("llama_jni")
        val resourcePath = "/native/$platform/$libFileName"

        val input = object {}.javaClass.getResourceAsStream(resourcePath)
            ?: error("Native library not found in resources: $resourcePath")

        val out = File.createTempFile("llama_jni_", libFileName)
        out.deleteOnExit()
        input.use { it.copyTo(out.outputStream()) }

        System.load(out.absolutePath)
    }
}
