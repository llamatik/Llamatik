package com.llamatik.core.platform

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object MultimodalBridge {
    init {
        System.loadLibrary("llama_jni")
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
}
