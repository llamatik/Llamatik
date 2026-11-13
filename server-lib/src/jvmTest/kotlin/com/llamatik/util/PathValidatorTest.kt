package com.llamatik.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PathValidatorTest {

    // ── validateModelPath ────────────────────────────────────────────────────

    @Test
    fun `valid path is returned as normalized absolute string`() {
        val result = PathValidator.validateModelPath("/models/test.gguf")
        // Result is absolute and ends with the expected filename
        assertTrue(result.endsWith("test.gguf"), "Expected path to end with test.gguf, got: $result")
    }

    @Test
    fun `path with null byte throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            PathValidator.validateModelPath("/models/\u0000evil.gguf")
        }
    }

    @Test
    fun `path with null byte in session path throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            PathValidator.validateSessionPath("/sessions/\u0000evil.bin")
        }
    }

    @Test
    fun `relative path with traversal components is resolved to absolute path`() {
        // Without MODELS_DIR set, traversal paths are normalized but not rejected.
        // Verify normalization actually happens (no double-dots remain).
        val result = PathValidator.validateModelPath("../../etc/passwd")
        assertTrue(!result.contains(".."), "Normalized path should not contain '..', got: $result")
    }

    // ── sanitizeError ────────────────────────────────────────────────────────

    @Test
    fun `sanitizeError strips Unix absolute paths from message`() {
        val raw = "Failed to load /home/user/models/my-model.gguf: no such file"
        val sanitized = PathValidator.sanitizeError(raw)
        assertTrue(!sanitized.contains("/home"), "Unix path should be stripped: $sanitized")
        assertTrue(sanitized.contains("[path]"), "Placeholder should be present: $sanitized")
    }

    @Test
    fun `sanitizeError strips Windows absolute paths from message`() {
        val raw = "Cannot open C:\\Users\\Ferran\\models\\model.gguf"
        val sanitized = PathValidator.sanitizeError(raw)
        assertTrue(!sanitized.contains("C:\\"), "Windows path should be stripped: $sanitized")
        assertTrue(sanitized.contains("[path]"), "Placeholder should be present: $sanitized")
    }

    @Test
    fun `sanitizeError returns operation failed for null message`() {
        assertEquals("Operation failed", PathValidator.sanitizeError(null))
    }

    @Test
    fun `sanitizeError caps message at 256 characters`() {
        val long = "Error: " + "x".repeat(300)
        val sanitized = PathValidator.sanitizeError(long)
        assertTrue(sanitized.length <= 256, "Message should be capped at 256 chars, was ${sanitized.length}")
    }

    @Test
    fun `sanitizedMessage extension delegates to sanitizeError`() {
        val ex = RuntimeException("/very/secret/internal/path failed")
        val sanitized = ex.sanitizedMessage()
        assertTrue(!sanitized.contains("/very/secret"), "Path should be stripped: $sanitized")
        assertTrue(sanitized.contains("[path]"), "Placeholder should be present: $sanitized")
    }

    @Test
    fun `sanitizeError does not modify messages without paths`() {
        val raw = "model not loaded"
        val sanitized = PathValidator.sanitizeError(raw)
        assertEquals(raw, sanitized)
    }
}
