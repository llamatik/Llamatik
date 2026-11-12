@file:OptIn(ExperimentalForeignApi::class)

package com.llamatik.core.platform

import com.llamatik.core.platform.sd.vu_free
import com.llamatik.core.platform.sd.vu_init
import com.llamatik.core.platform.sd.vu_is_loaded
import com.llamatik.core.platform.sd.vu_release
import com.llamatik.core.platform.sd.vu_upscale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pin
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value

actual object VideoUpscalerBridge {

    actual fun initModel(
        diffusionModelPath: String,
        vaePath: String,
        upscaleFactor: Int,
        threads: Int,
    ): Boolean = vu_init(diffusionModelPath, vaePath, upscaleFactor, threads) != 0

    actual fun isLoaded(): Boolean = vu_is_loaded() != 0

    actual fun upscale(
        packedFrames: ByteArray,
        seed: Long,
        windowSize: Int,
        temporalChunkSize: Int,
    ): ByteArray {
        if (!isLoaded() || packedFrames.size < 16) return ByteArray(0)

        val pinned = packedFrames.pin()
        return try {
            memScoped {
                val outSize = alloc<IntVar>()
                val ptr = vu_upscale(
                    packed = pinned.addressOf(0).reinterpret(),
                    packed_size = packedFrames.size,
                    seed = seed,
                    window_size = windowSize,
                    temporal_chunk_size = temporalChunkSize,
                    out_total_size = outSize.ptr
                ) ?: return@memScoped ByteArray(0)

                val size = outSize.value
                if (size <= 0) {
                    vu_free(ptr)
                    return@memScoped ByteArray(0)
                }
                val out = ptr.readBytes(size)
                vu_free(ptr)
                out
            }
        } finally {
            pinned.unpin()
        }
    }

    actual fun release() = vu_release()
}
