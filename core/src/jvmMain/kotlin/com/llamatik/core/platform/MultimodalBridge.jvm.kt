package com.llamatik.core.platform

actual object MultimodalBridge {
    init {
        // Reuse the same JNI shared library that contains all other bridge exports.
        loadNativeFromResources()
    }

    actual external fun initModel(modelPath: String, mmprojPath: String): Boolean

    private external fun nativeAnalyzeImageBytesStream(
        imageBytes: ByteArray,
        prompt: String,
        callback: GenStream
    )

    private external fun nativeGetCapabilities(): Int

    actual fun getCapabilities(): MultimodalCapabilities =
        MultimodalCapabilities.fromMask(nativeGetCapabilities())

    actual external fun cancelAnalysis()
    actual external fun release()

    actual fun analyzeImageBytesStream(imageBytes: ByteArray, prompt: String, callback: GenStream) {
        nativeAnalyzeImageBytesStream(imageBytes, prompt, callback)
    }

    private fun loadNativeFromResources() {
        DesktopNativeLoader.loadJni()
    }
}
