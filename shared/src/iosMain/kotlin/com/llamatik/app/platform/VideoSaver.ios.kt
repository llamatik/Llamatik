@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.llamatik.app.platform

import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.OpaqueValue
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVAssetWriter
import platform.AVFoundation.AVAssetWriterInput
import platform.AVFoundation.AVAssetWriterInputPixelBufferAdaptor
import platform.AVFoundation.AVAssetWriterStatusCompleted
import platform.AVFoundation.AVFileTypeMPEG4
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeH264
import platform.AVFoundation.AVVideoHeightKey
import platform.AVFoundation.AVVideoWidthKey
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.kCMTimeZero
import platform.CoreVideo.CVPixelBufferCreateWithBytes
import platform.CoreVideo.CVPixelBufferRelease
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import platform.CoreVideo.kCVReturnSuccess
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

actual object VideoSaver {

    actual suspend fun saveVideo(
        frames: List<ByteArray>,
        width: Int,
        height: Int,
        fps: Int
    ): Boolean {
        if (frames.isEmpty()) return false
        return withContext(Dispatchers.Default) {
            try {
                val timestamp = (NSDate().timeIntervalSince1970 * 1000).toLong()
                val tmpPath = "${NSTemporaryDirectory()}llamatik_video_$timestamp.mp4"
                NSFileManager.defaultManager.removeItemAtPath(tmpPath, null)
                val tmpUrl = NSURL.fileURLWithPath(tmpPath)

                val encoded = encodeFramesToMp4(tmpUrl, frames, width, height, fps.coerceAtLeast(1))
                if (!encoded) return@withContext false

                saveToPhotosLibrary(tmpUrl)
            } catch (_: Throwable) {
                false
            }
        }
    }

    private suspend fun encodeFramesToMp4(
        outputUrl: NSURL,
        frames: List<ByteArray>,
        width: Int,
        height: Int,
        fps: Int
    ): Boolean {
        val assetWriter: AVAssetWriter = memScoped {
            val errRef = alloc<ObjCObjectVar<NSError?>>()
            AVAssetWriter(URL = outputUrl, fileType = AVFileTypeMPEG4, error = errRef.ptr)
        } ?: return false

        val videoSettings = mapOf<Any?, Any?>(
            AVVideoCodecKey to AVVideoCodecTypeH264,
            AVVideoWidthKey to NSNumber(int = width),
            AVVideoHeightKey to NSNumber(int = height)
        )

        val writerInput = AVAssetWriterInput(
            mediaType = AVMediaTypeVideo,
            outputSettings = videoSettings
        )
        writerInput.expectsMediaDataInRealTime = false

        val pbAttrs = mapOf<Any?, Any?>(
            platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey to NSNumber(unsignedInt = kCVPixelFormatType_32BGRA),
            platform.CoreVideo.kCVPixelBufferWidthKey to NSNumber(int = width),
            platform.CoreVideo.kCVPixelBufferHeightKey to NSNumber(int = height)
        )

        val adaptor = AVAssetWriterInputPixelBufferAdaptor(
            assetWriterInput = writerInput,
            sourcePixelBufferAttributes = pbAttrs
        )

        if (!assetWriter.canAddInput(writerInput)) return false
        assetWriter.addInput(writerInput)
        if (!assetWriter.startWriting()) return false
        assetWriter.startSessionAtSourceTime(kCMTimeZero)

        for ((idx, frameData) in frames.withIndex()) {
            var retries = 0
            while (!writerInput.isReadyForMoreMediaData && retries < 200) {
                delay(5)
                retries++
            }
            if (!writerInput.isReadyForMoreMediaData) continue

            val pts = CMTimeMake(value = idx.toLong(), timescale = fps)
            appendBgraFrame(adaptor, frameData, width, height, pts)
        }

        writerInput.markAsFinished()

        return suspendCancellableCoroutine { cont ->
            assetWriter.finishWritingWithCompletionHandler {
                cont.resume(assetWriter.status == AVAssetWriterStatusCompleted)
            }
            cont.invokeOnCancellation { assetWriter.cancelWriting() }
        }
    }

    private fun appendBgraFrame(
        adaptor: AVAssetWriterInputPixelBufferAdaptor,
        rgba: ByteArray,
        width: Int,
        height: Int,
        pts: platform.CoreMedia.CMTime
    ) {
        val bgra = ByteArray(rgba.size)
        val pixelCount = minOf(rgba.size / 4, width * height)
        for (i in 0 until pixelCount) {
            val o = i * 4
            bgra[o]     = rgba[o + 2]
            bgra[o + 1] = rgba[o + 1]
            bgra[o + 2] = rgba[o]
            bgra[o + 3] = rgba[o + 3]
        }

        bgra.usePinned { pinned ->
            memScoped {
                val pbRef = alloc<CPointerVar<OpaqueValue>>()
                val status = CVPixelBufferCreateWithBytes(
                    null,
                    width.convert(),
                    height.convert(),
                    kCVPixelFormatType_32BGRA,
                    pinned.addressOf(0),
                    (width * 4).convert(),
                    null, null, null,
                    pbRef.ptr
                )
                if (status == kCVReturnSuccess) {
                    val pb = pbRef.value ?: return@memScoped
                    adaptor.appendPixelBuffer(pb, withPresentationTime = pts)
                    CVPixelBufferRelease(pb)
                }
            }
        }
    }

    private suspend fun saveToPhotosLibrary(videoUrl: NSURL): Boolean {
        return suspendCancellableCoroutine { cont ->
            PHPhotoLibrary.requestAuthorization { status ->
                if (status != PHAuthorizationStatusAuthorized) {
                    NSFileManager.defaultManager.removeItemAtURL(videoUrl, null)
                    cont.resume(false)
                    return@requestAuthorization
                }

                var assetId: String? = null
                PHPhotoLibrary.sharedPhotoLibrary().performChangesAndWait({
                    val req = PHAssetChangeRequest.creationRequestForAssetFromVideoAtFileURL(videoUrl)
                    assetId = req?.placeholderForCreatedAsset?.localIdentifier
                }, null)

                NSFileManager.defaultManager.removeItemAtURL(videoUrl, null)
                cont.resume(assetId != null)
            }
        }
    }
}
