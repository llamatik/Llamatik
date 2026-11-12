package com.llamatik.util

import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * Validates caller-supplied filesystem paths before they reach native JNI bridges.
 *
 * Configured via environment variables:
 * - `MODELS_DIR`   — allowed base directory for model files, WAV files, and companion files.
 *                    All caller-supplied paths must resolve within this directory.
 *                    When not set, path containment is not enforced (suitable for
 *                    fully trusted LAN-only deployments).
 * - `SESSIONS_DIR` — allowed base directory for KV-session save/load paths.
 *                    Defaults to `$MODELS_DIR/sessions` when `MODELS_DIR` is configured.
 *                    When neither is set, session path containment is not enforced.
 *
 * In all cases paths are resolved to their canonical absolute form and checked for
 * null-bytes and traversal components, regardless of the allowlist setting.
 *
 * On validation failure an [IllegalArgumentException] is thrown with a safe message.
 * The `StatusPages` plugin in `GeneralRoutingConfig` catches that exception and
 * returns HTTP 400, so route handlers require no per-call try-catch.
 */
object PathValidator {

    private fun envPath(name: String): Path? = System.getenv(name)
        ?.takeIf { it.isNotBlank() }
        ?.let { Path.of(it).toAbsolutePath().normalize() }

    @Volatile
    private var modelsDir: Path? = envPath("MODELS_DIR")

    @Volatile
    private var sessionsDir: Path? = envPath("SESSIONS_DIR") ?: modelsDir?.resolve("sessions")

    /**
     * Overrides the environment-derived directories. Embedding hosts (e.g. the Llamatik app)
     * cannot set environment variables, so they configure containment explicitly through
     * [com.llamatik.LlamatikServerConfig]. `null` disables containment for that kind of path.
     */
    fun configure(modelsDir: String?, sessionsDir: String?) {
        val models = modelsDir?.takeIf { it.isNotBlank() }?.let { Path.of(it).toAbsolutePath().normalize() }
        this.modelsDir = models
        this.sessionsDir = sessionsDir?.takeIf { it.isNotBlank() }
            ?.let { Path.of(it).toAbsolutePath().normalize() }
            ?: models?.resolve("sessions")
    }

    /**
     * Validates a model/WAV/companion-file path.
     * Returns the normalized absolute path string on success.
     * Throws [IllegalArgumentException] with a safe message on any violation.
     */
    fun validateModelPath(raw: String): String = validate(raw, modelsDir, "model")

    /**
     * Validates a session-file path (save/load).
     * Returns the normalized absolute path string on success.
     * Throws [IllegalArgumentException] with a safe message on any violation.
     */
    fun validateSessionPath(raw: String): String = validate(raw, sessionsDir, "session")

    private fun validate(raw: String, allowedDir: Path?, label: String): String {
        if (raw.contains('\u0000')) throw IllegalArgumentException("Invalid $label path")

        val resolved = try {
            Path.of(raw).toAbsolutePath().normalize()
        } catch (e: InvalidPathException) {
            throw IllegalArgumentException("Invalid $label path")
        }

        if (allowedDir != null && !resolved.startsWith(allowedDir)) {
            throw IllegalArgumentException("$label path is outside the allowed directory")
        }

        return resolved.toString()
    }

    /**
     * Strips absolute filesystem paths from native exception messages so that
     * server-internal paths are never returned verbatim to HTTP callers.
     *
     * A sanitized, length-capped message is returned; the full original message
     * should be logged server-side before this function is called.
     */
    fun sanitizeError(message: String?): String {
        if (message == null) return "Operation failed"
        // Replace Unix-style (/foo/bar) and Windows-style (C:\foo\bar) paths.
        val sanitized = message
            .replace(Regex("""[A-Za-z]:[/\\][^\s"',;]*"""), "[path]")   // Windows
            .replace(Regex("""/[^\s"',;]+"""), "[path]")                 // Unix
            .take(256)
        return sanitized.ifBlank { "Operation failed" }
    }
}

/**
 * Returns a sanitized version of the throwable's message, safe to send to callers.
 * Always log the original [Throwable] before calling this.
 */
fun Throwable.sanitizedMessage(): String = PathValidator.sanitizeError(this.message)
