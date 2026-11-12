package com.llamatik.routes

import com.llamatik.fakes.FakeLlamaService
import com.llamatik.fakes.FakeStableDiffusionService
import com.llamatik.withTestApp
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatusRoutesTest {

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `GET models returns all bridges unloaded on a fresh server`() = withTestApp {
        val body = client.get("/v1/models").bodyAsJson()

        assertBridge(body, "generation", loaded = false, model = null)
        assertBridge(body, "embedding",  loaded = false, model = null)
        assertBridge(body, "whisper",    loaded = false, model = null)
        assertBridge(body, "mtp",        loaded = false, model = null)

        val sd = body["stableDiffusion"]!!.jsonObject
        assertEquals("false", sd["loaded"]?.jsonPrimitive?.content)
        assertJsonNull(sd, "model")

        val mm = body["multimodal"]!!.jsonObject
        assertEquals("false", mm["loaded"]?.jsonPrimitive?.content)
        assertJsonNull(mm, "model")
    }

    // ── generation ────────────────────────────────────────────────────────────

    @Test
    fun `generation shows loaded after init`() = withTestApp {
        client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/llama.gguf"}""")
        }
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "generation", loaded = true, model = "llama.gguf")
        assertBridge(body, "embedding",  loaded = false, model = null)
    }

    @Test
    fun `generation capabilities are populated after init`() = withTestApp {
        // FakeLlamaService returns finetuneType="instruct" and chatTemplate="{{messages}}" by default
        client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/llama.gguf"}""")
        }
        val caps = client.get("/v1/models").bodyAsJson()["generation"]!!
            .jsonObject["capabilities"]!!.jsonObject
        assertEquals("instruct", caps["finetuneType"]?.jsonPrimitive?.content)
        assertEquals("true",     caps["hasChatTemplate"]?.jsonPrimitive?.content)
    }

    @Test
    fun `generation capabilities are null when model is not loaded`() = withTestApp {
        val gen = client.get("/v1/models").bodyAsJson()["generation"]!!.jsonObject
        assertJsonNull(gen, "capabilities")
    }

    @Test
    fun `generation capabilities reflect base model with no chat template`() = withTestApp(
        llama = FakeLlamaService(
            finetuneTypeResult = Result.success("base"),
            chatTemplateResult = Result.success(null),
        )
    ) {
        client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/base.gguf"}""")
        }
        val caps = client.get("/v1/models").bodyAsJson()["generation"]!!
            .jsonObject["capabilities"]!!.jsonObject
        assertEquals("base",  caps["finetuneType"]?.jsonPrimitive?.content)
        assertEquals("false", caps["hasChatTemplate"]?.jsonPrimitive?.content)
    }

    @Test
    fun `generation shows unloaded after shutdown`() = withTestApp {
        client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/llama.gguf"}""")
        }
        client.post("/v1/generation/shutdown")
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "generation", loaded = false, model = null)
    }

    @Test
    fun `generation stays unloaded when init fails`() = withTestApp(
        llama = FakeLlamaService(initGenerateResult = Result.failure(RuntimeException("fail")))
    ) {
        client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/bad.gguf"}""")
        }
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "generation", loaded = false, model = null)
    }

    // ── embedding ─────────────────────────────────────────────────────────────

    @Test
    fun `embedding shows loaded after init`() = withTestApp {
        client.post("/v1/embeddings/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/nomic-embed.gguf"}""")
        }
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "embedding",  loaded = true, model = "nomic-embed.gguf")
        assertBridge(body, "generation", loaded = false, model = null)
    }

    // ── whisper ───────────────────────────────────────────────────────────────

    @Test
    fun `whisper shows loaded after init`() = withTestApp {
        client.post("/v1/whisper/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/ggml-tiny.bin"}""")
        }
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "whisper", loaded = true, model = "ggml-tiny.bin")
    }

    @Test
    fun `whisper shows unloaded after release`() = withTestApp {
        client.post("/v1/whisper/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/ggml-tiny.bin"}""")
        }
        client.post("/v1/whisper/release")
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "whisper", loaded = false, model = null)
    }

    // ── stable diffusion ──────────────────────────────────────────────────────

    @Test
    fun `stable diffusion shows loaded after init`() = withTestApp {
        // FakeStableDiffusionService.supportsVideoGeneration() returns true by default
        client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/dreamshaper.gguf"}""")
        }
        val sd = client.get("/v1/models").bodyAsJson()["stableDiffusion"]!!.jsonObject
        assertEquals("true",             sd["loaded"]?.jsonPrimitive?.content)
        assertEquals("dreamshaper.gguf", sd["model"]?.jsonPrimitive?.content)
        assertEquals("true",             sd["supportsVideo"]?.jsonPrimitive?.content)
        assertJsonNull(sd, "vae")
    }

    @Test
    fun `stable diffusion supportsVideo is false for image-only models`() = withTestApp(
        sd = FakeStableDiffusionService(supportsVideoResult = Result.success(false))
    ) {
        client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/flux.gguf"}""")
        }
        val sd = client.get("/v1/models").bodyAsJson()["stableDiffusion"]!!.jsonObject
        assertEquals("false", sd["supportsVideo"]?.jsonPrimitive?.content)
    }

    @Test
    fun `stable diffusion supportsVideo is null when not loaded`() = withTestApp {
        val sd = client.get("/v1/models").bodyAsJson()["stableDiffusion"]!!.jsonObject
        assertJsonNull(sd, "supportsVideo")
    }

    @Test
    fun `stable diffusion exposes companion filenames`() = withTestApp {
        client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"modelPath":"/models/wan2.1.gguf","vaePath":"/models/vae.safetensors","t5xxlPath":"/models/t5xxl.gguf"}"""
            )
        }
        val sd = client.get("/v1/models").bodyAsJson()["stableDiffusion"]!!.jsonObject
        assertEquals("wan2.1.gguf",       sd["model"]?.jsonPrimitive?.content)
        assertEquals("vae.safetensors",   sd["vae"]?.jsonPrimitive?.content)
        assertEquals("t5xxl.gguf",        sd["t5xxl"]?.jsonPrimitive?.content)
        assertJsonNull(sd, "clipVision")
    }

    @Test
    fun `stable diffusion shows unloaded after release`() = withTestApp {
        client.post("/v1/stablediffusion/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/dreamshaper.gguf"}""")
        }
        client.post("/v1/stablediffusion/release")
        val sd = client.get("/v1/models").bodyAsJson()["stableDiffusion"]!!.jsonObject
        assertEquals("false", sd["loaded"]?.jsonPrimitive?.content)
        assertJsonNull(sd, "model")
    }

    // ── multimodal ────────────────────────────────────────────────────────────

    @Test
    fun `multimodal shows loaded after init`() = withTestApp {
        client.post("/v1/multimodal/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/smolvlm.gguf","mmprojPath":"/models/mmproj.gguf"}""")
        }
        val mm = client.get("/v1/models").bodyAsJson()["multimodal"]!!.jsonObject
        assertEquals("true",        mm["loaded"]?.jsonPrimitive?.content)
        assertEquals("smolvlm.gguf", mm["model"]?.jsonPrimitive?.content)
        assertEquals("mmproj.gguf",  mm["mmproj"]?.jsonPrimitive?.content)
    }

    @Test
    fun `multimodal shows unloaded after release`() = withTestApp {
        client.post("/v1/multimodal/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/smolvlm.gguf","mmprojPath":"/models/mmproj.gguf"}""")
        }
        client.post("/v1/multimodal/release")
        val mm = client.get("/v1/models").bodyAsJson()["multimodal"]!!.jsonObject
        assertEquals("false", mm["loaded"]?.jsonPrimitive?.content)
    }

    // ── MTP ───────────────────────────────────────────────────────────────────

    @Test
    fun `mtp shows loaded after init`() = withTestApp {
        client.post("/v1/generation/mtp/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/qwen3.gguf"}""")
        }
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "mtp", loaded = true, model = "qwen3.gguf")
    }

    @Test
    fun `mtp shows unloaded after shutdown`() = withTestApp {
        client.post("/v1/generation/mtp/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/qwen3.gguf"}""")
        }
        client.post("/v1/generation/mtp/shutdown")
        val body = client.get("/v1/models").bodyAsJson()
        assertBridge(body, "mtp", loaded = false, model = null)
    }

    // ── 200 status ────────────────────────────────────────────────────────────

    @Test
    fun `GET models returns 200`() = withTestApp {
        assertEquals(HttpStatusCode.OK, client.get("/v1/models").status)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun io.ktor.client.statement.HttpResponse.bodyAsJson() =
        Json.parseToJsonElement(bodyAsText()).jsonObject

    /**
     * Asserts that [key] in [obj] is JSON null (or absent).
     *
     * In kotlinx.serialization, a `null` field serializes as `JsonNull`, not Kotlin null.
     * `JsonNull.jsonPrimitive.content` returns the string `"null"`, not null — so a plain
     * `assertNull(obj[key]?.jsonPrimitive?.content)` would incorrectly fail.
     */
    private fun assertJsonNull(obj: JsonObject, key: String) {
        val element = obj[key]
        assertTrue(
            element == null || element is JsonNull,
            "Expected '$key' to be JSON null, got: $element",
        )
    }

    private fun assertBridge(
        root: JsonObject,
        key: String,
        loaded: Boolean,
        model: String?,
    ) {
        val obj = root[key]!!.jsonObject
        assertEquals(loaded.toString(), obj["loaded"]?.jsonPrimitive?.content, "$key.loaded")
        if (model == null) assertJsonNull(obj, "model")
        else assertEquals(model, obj["model"]?.jsonPrimitive?.content, "$key.model")
    }
}
