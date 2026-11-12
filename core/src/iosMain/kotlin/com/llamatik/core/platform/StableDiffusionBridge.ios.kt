@file:OptIn(ExperimentalForeignApi::class)

package com.llamatik.core.platform

import com.llamatik.core.platform.sd.sd_bridge_set_progress_callback
import com.llamatik.core.platform.sd.sd_free_bytes
import com.llamatik.core.platform.sd.sd_free_video_frames
import com.llamatik.core.platform.sd.sd_generate_with_control_rgba
import com.llamatik.core.platform.sd.sd_has_control_net
import com.llamatik.core.platform.sd.sd_img2img_rgba
import com.llamatik.core.platform.sd.sd_init
import com.llamatik.core.platform.sd.sd_load_control_net
import com.llamatik.core.platform.sd.sd_release
import com.llamatik.core.platform.sd.sd_supports_video_generation
import com.llamatik.core.platform.sd.sd_txt2img_rgba
import com.llamatik.core.platform.sd.sd_txt2video_frames
import com.llamatik.core.platform.sd.sd_unload_control_net
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pin
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.Foundation.NSLog

actual object StableDiffusionBridge {

    private var isInitialized = false

    actual fun getModelPath(modelFileName: String): String {
        // iOS app manages download/path. Keep consistent with your other bridges.
        return modelFileName
    }

    actual fun initModel(
        modelPath: String, threads: Int,
        vaePath: String?, t5xxlPath: String?, clipVisionPath: String?,
    ): Boolean {
        release()
        val ok = sd_init(modelPath, threads, vaePath ?: "", t5xxlPath ?: "", clipVisionPath ?: "") != 0
        isInitialized = ok
        if (!ok) NSLog("[StableDiffusionBridge] sd_init FAILED path=$modelPath")
        else NSLog("[StableDiffusionBridge] sd_init OK")
        return ok
    }

    actual fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray {
        if (!isInitialized) return ByteArray(0)
        if (prompt.isBlank()) return ByteArray(0)

        return memScoped {
            val outW = alloc<IntVar>()
            val outH = alloc<IntVar>()
            val outSize = alloc<IntVar>()

            val bytesPtr = sd_txt2img_rgba(
                prompt = prompt,
                negative_prompt = (negativePrompt ?: ""),
                width = width,
                height = height,
                steps = steps,
                cfg_scale = cfgScale,
                seed = seed,
                out_w = outW.ptr,
                out_h = outH.ptr,
                out_size_bytes = outSize.ptr
            ) ?: return@memScoped ByteArray(0)

            val size = outSize.value
            if (size <= 0) {
                sd_free_bytes(bytesPtr)
                return@memScoped ByteArray(0)
            }

            val out = bytesPtr.readBytes(size)
            sd_free_bytes(bytesPtr)
            out
        }
    }

    actual fun img2img(
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
    ): ByteArray {
        if (!isInitialized) return ByteArray(0)
        if (prompt.isBlank()) return ByteArray(0)
        if (initImageRgba.isEmpty() || initImageW <= 0 || initImageH <= 0) return ByteArray(0)

        val pinned = initImageRgba.pin()
        return try {
            memScoped {
                val outW = alloc<IntVar>()
                val outH = alloc<IntVar>()
                val outSize = alloc<IntVar>()

                val bytesPtr = sd_img2img_rgba(
                    init_image_rgba = pinned.addressOf(0).reinterpret(),
                    init_image_w = initImageW,
                    init_image_h = initImageH,
                    prompt = prompt,
                    negative_prompt = (negativePrompt ?: ""),
                    width = width,
                    height = height,
                    steps = steps,
                    cfg_scale = cfgScale,
                    strength = strength,
                    seed = seed,
                    out_w = outW.ptr,
                    out_h = outH.ptr,
                    out_size_bytes = outSize.ptr
                ) ?: return@memScoped ByteArray(0)

                val size = outSize.value
                if (size <= 0) {
                    sd_free_bytes(bytesPtr)
                    return@memScoped ByteArray(0)
                }

                val out = bytesPtr.readBytes(size)
                sd_free_bytes(bytesPtr)
                out
            }
        } finally {
            pinned.unpin()
        }
    }

    actual fun release() {
        if (isInitialized) {
            sd_release()
        }
        isInitialized = false
    }

    actual fun supportsVideoGeneration(): Boolean {
        if (!isInitialized) return false
        return sd_supports_video_generation() != 0
    }

    actual fun txt2video(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        videoFrames: Int,
        fps: Int,
        cfgScale: Float,
        seed: Long,
    ): ByteArray {
        if (!isInitialized) return ByteArray(0)
        if (prompt.isBlank()) return ByteArray(0)

        return memScoped {
            val outSize = alloc<IntVar>()

            val bytesPtr = sd_txt2video_frames(
                prompt = prompt,
                negative_prompt = (negativePrompt ?: ""),
                width = width,
                height = height,
                video_frames = videoFrames,
                fps = fps,
                cfg_scale = cfgScale,
                seed = seed,
                out_total_size = outSize.ptr
            ) ?: return@memScoped ByteArray(0)

            val size = outSize.value
            if (size <= 0) {
                sd_free_video_frames(bytesPtr)
                return@memScoped ByteArray(0)
            }

            val out = bytesPtr.readBytes(size)
            sd_free_video_frames(bytesPtr)
            out
        }
    }

    // ---- progress ------------------------------------------------------------------------
    private var progressRef: StableRef<SdProgressListener>? = null

    actual fun setProgressListener(listener: SdProgressListener?) {
        // Clear the native callback before disposing the ref it points to.
        sd_bridge_set_progress_callback(null, null)
        progressRef?.dispose()
        progressRef = null
        if (listener == null) return

        val ref = StableRef.create(listener)
        progressRef = ref
        sd_bridge_set_progress_callback(
            staticCFunction { stage: CPointer<ByteVar>?,
                              detail: CPointer<ByteVar>?,
                              unit: CPointer<ByteVar>?,
                              samplePass: Int,
                              completed: Long,
                              total: Long,
                              user: COpaquePointer? ->
                val l = user?.asStableRef<SdProgressListener>()?.get() ?: return@staticCFunction
                l.onProgress(
                    SdProgress(
                        stage = stage?.toKString() ?: "",
                        detail = detail?.toKString(),
                        unit = unit?.toKString(),
                        samplePass = samplePass,
                        completed = completed,
                        total = total,
                    )
                )
            },
            ref.asCPointer()
        )
    }

    // ---- ControlNet ----------------------------------------------------------------------
    actual fun loadControlNet(path: String): Boolean {
        if (!isInitialized) return false
        return sd_load_control_net(path) != 0
    }

    actual fun unloadControlNet(): Boolean {
        if (!isInitialized) return false
        return sd_unload_control_net() != 0
    }

    actual fun hasControlNet(): Boolean {
        if (!isInitialized) return false
        return sd_has_control_net() != 0
    }

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
    ): ByteArray {
        if (!isInitialized) return ByteArray(0)
        if (prompt.isBlank()) return ByteArray(0)
        if (controlImageRgba.isEmpty() || controlImageW <= 0 || controlImageH <= 0) return ByteArray(0)
        if (controlImageRgba.size < controlImageW * controlImageH * 4) return ByteArray(0)

        val pinned = controlImageRgba.pin()
        return try {
            memScoped {
                val outW = alloc<IntVar>()
                val outH = alloc<IntVar>()
                val outSize = alloc<IntVar>()

                val bytesPtr = sd_generate_with_control_rgba(
                    control_image_rgba = pinned.addressOf(0).reinterpret(),
                    control_w = controlImageW,
                    control_h = controlImageH,
                    control_type = controlType.nativeId,
                    prompt = prompt,
                    negative_prompt = (negativePrompt ?: ""),
                    width = width,
                    height = height,
                    steps = steps,
                    cfg_scale = cfgScale,
                    control_strength = controlStrength,
                    seed = seed,
                    out_w = outW.ptr,
                    out_h = outH.ptr,
                    out_size_bytes = outSize.ptr
                ) ?: return@memScoped ByteArray(0)

                val size = outSize.value
                if (size <= 0) {
                    sd_free_bytes(bytesPtr)
                    return@memScoped ByteArray(0)
                }
                val out = bytesPtr.readBytes(size)
                sd_free_bytes(bytesPtr)
                out
            }
        } finally {
            pinned.unpin()
        }
    }
}
