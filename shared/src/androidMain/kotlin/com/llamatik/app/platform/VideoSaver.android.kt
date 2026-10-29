@file:Suppress("DEPRECATION")

package com.llamatik.app.platform

import android.content.ContentValues
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual object VideoSaver {

    actual suspend fun saveVideo(
        frames: List<ByteArray>,
        width: Int,
        height: Int,
        fps: Int
    ): Boolean {
        if (frames.isEmpty()) return false
        return withContext(Dispatchers.IO) {
            try {
                val ctx = AndroidContextHolder.appContext
                val tmp = File(ctx.cacheDir, "llamatik_video_${System.currentTimeMillis()}.mp4")
                encodeToMp4(tmp, frames, width, height, fps.coerceAtLeast(1))
                val saved = insertIntoMediaStore(ctx, tmp)
                tmp.delete()
                saved
            } catch (_: Throwable) {
                false
            }
        }
    }

    private fun encodeToMp4(
        output: File,
        frames: List<ByteArray>,
        width: Int,
        height: Int,
        fps: Int
    ) {
        val encW = if (width % 2 == 0) width else width + 1
        val encH = if (height % 2 == 0) height else height + 1
        val bitrate = (encW * encH * fps / 10).coerceIn(500_000, 8_000_000)
        val frameDurationUs = 1_000_000L / fps

        val format = MediaFormat.createVideoFormat("video/avc", encW, encH).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val codec = MediaCodec.createEncoderByType("video/avc")
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        val muxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false
        val bufInfo = MediaCodec.BufferInfo()

        var frameIdx = 0
        var eosSent = false
        var done = false

        try {
            while (!done) {
                if (!eosSent) {
                    val inputIdx = codec.dequeueInputBuffer(10_000L)
                    if (inputIdx >= 0) {
                        val buf = codec.getInputBuffer(inputIdx)!!
                        buf.clear()
                        if (frameIdx < frames.size) {
                            val yuv = rgbaToNv12(frames[frameIdx], encW, encH, width, height)
                            buf.put(yuv)
                            codec.queueInputBuffer(inputIdx, 0, yuv.size, frameIdx * frameDurationUs, 0)
                            frameIdx++
                        } else {
                            codec.queueInputBuffer(inputIdx, 0, 0, frameIdx * frameDurationUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            eosSent = true
                        }
                    }
                }

                val outputIdx = codec.dequeueOutputBuffer(bufInfo, 10_000L)
                when {
                    outputIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    outputIdx >= 0 -> {
                        if (bufInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufInfo.size = 0
                        }
                        if (bufInfo.size > 0 && muxerStarted) {
                            val outBuf = codec.getOutputBuffer(outputIdx)!!
                            outBuf.position(bufInfo.offset)
                            outBuf.limit(bufInfo.offset + bufInfo.size)
                            muxer.writeSampleData(trackIndex, outBuf, bufInfo)
                        }
                        codec.releaseOutputBuffer(outputIdx, false)
                        if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            done = true
                        }
                    }
                }
            }
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
            if (muxerStarted) runCatching { muxer.stop() }
            runCatching { muxer.release() }
        }
    }

    // BT.601 RGBA → NV12 (YUV420SemiPlanar) with optional padding to encW×encH
    private fun rgbaToNv12(rgba: ByteArray, encW: Int, encH: Int, srcW: Int, srcH: Int): ByteArray {
        val yuv = ByteArray(encW * encH * 3 / 2)
        val uvOff = encW * encH

        for (row in 0 until encH) {
            val srcRow = row.coerceAtMost(srcH - 1)
            for (col in 0 until encW) {
                val srcCol = col.coerceAtMost(srcW - 1)
                val s = (srcRow * srcW + srcCol) * 4
                if (s + 2 >= rgba.size) continue

                val r = rgba[s].toInt() and 0xFF
                val g = rgba[s + 1].toInt() and 0xFF
                val b = rgba[s + 2].toInt() and 0xFF

                yuv[row * encW + col] =
                    (((66 * r + 129 * g + 25 * b + 128) shr 8) + 16).coerceIn(16, 235).toByte()

                if (row % 2 == 0 && col % 2 == 0) {
                    val uvIdx = uvOff + (row / 2) * encW + col
                    if (uvIdx + 1 < yuv.size) {
                        yuv[uvIdx] =
                            (((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128).coerceIn(16, 240).toByte()
                        yuv[uvIdx + 1] =
                            (((112 * r - 94 * g - 18 * b + 128) shr 8) + 128).coerceIn(16, 240).toByte()
                    }
                }
            }
        }
        return yuv
    }

    private fun insertIntoMediaStore(ctx: android.content.Context, file: File): Boolean {
        val fileName = "llamatik_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            values.put(MediaStore.Video.Media.IS_PENDING, 1)
            val uri = ctx.contentResolver.insert(
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values
            ) ?: return false
            ctx.contentResolver.openOutputStream(uri)?.use { file.inputStream().copyTo(it) }
                ?: return false
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            ctx.contentResolver.update(uri, values, null, null)
            true
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            dir.mkdirs()
            val dest = File(dir, fileName)
            file.copyTo(dest, overwrite = true)
            values.put(MediaStore.Video.Media.DATA, dest.absolutePath)
            ctx.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            true
        }
    }
}
