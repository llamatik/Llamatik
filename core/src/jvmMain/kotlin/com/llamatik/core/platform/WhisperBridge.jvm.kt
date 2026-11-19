package com.llamatik.core.platform

import java.nio.file.Files
import kotlin.io.path.Path

actual object WhisperBridge {
    init {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            // On Windows, re-loading the same DLL from another temp path fails after
            // LlamaBridge has already loaded it. Reuse LlamaBridge's initialization instead.
            LlamaBridge
        } else {
            loadNativeFromResources()
        }
    }

    actual fun getModelPath(modelFileName: String): String {
        // Desktop: expect models to exist on disk (downloaded).
        // Return as-is if caller passes an absolute path; otherwise resolve from working dir.
        val p = Path(modelFileName)
        return if (Files.exists(p)) p.toAbsolutePath().toString() else modelFileName
    }

    actual external fun initModel(modelPath: String): Boolean
    actual external fun transcribeWav(wavPath: String, language: String?, initialPrompt: String?): String
    actual external fun transcribeWavSegments(wavPath: String, language: String?, initialPrompt: String?, translate: Boolean, diarize: Boolean): String
    actual external fun release()

    private fun loadNativeFromResources() {
        DesktopNativeLoader.loadJni()
    }
}
