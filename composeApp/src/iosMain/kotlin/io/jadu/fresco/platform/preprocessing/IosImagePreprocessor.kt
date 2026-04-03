package io.jadu.fresco.platform.preprocessing

import io.jadu.fresco.platform.camera.CameraImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.posix.memcpy

/**
 * iOS implementation: decodes JPEG via [UIImage] (auto-applies EXIF rotation),
 * resizes shorter side to 256, center-crops to 224×224, and normalizes with
 * ImageNet mean/std into NCHW layout.
 *
 * Matches torchvision: Resize(256) → CenterCrop(224) → ToTensor → Normalize
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosImagePreprocessor : ImagePreprocessor {

    override suspend fun preprocess(image: CameraImage): ImageTensor = withContext(Dispatchers.IO) {
        val nsData = image.bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = image.bytes.size.toULong())
        }

        // UIImage auto-applies EXIF orientation from JPEG metadata
        val uiImage = UIImage.imageWithData(nsData)
            ?: throw IllegalArgumentException("Failed to decode image bytes")
        val cgImage = uiImage.CGImage
            ?: throw IllegalArgumentException("Failed to get CGImage")

        val rgbaBytes = resizeAndCenterCropToRGBA(cgImage)
        extractNormalizedTensor(rgbaBytes)
    }

    /**
     * Resizes shorter side to 256, center-crops to 224×224, returns raw RGBA bytes.
     */
    private fun resizeAndCenterCropToRGBA(cgImage: platform.CoreGraphics.CGImageRef): ByteArray {
        val srcW = CGImageGetWidth(cgImage).toInt()
        val srcH = CGImageGetHeight(cgImage).toInt()

        // Scale so shorter side = RESIZE_SIZE
        val scale = RESIZE_SIZE.toDouble() / minOf(srcW, srcH)
        val scaledW = (srcW * scale).toInt()
        val scaledH = (srcH * scale).toInt()

        // Draw into scaled bitmap context
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val scaledBytesPerRow = scaledW * 4
        val scaledContext = CGBitmapContextCreate(
            data = null,
            width = scaledW.toULong(),
            height = scaledH.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = scaledBytesPerRow.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value
        ) ?: throw IllegalStateException("Failed to create scaled bitmap context")

        CGContextDrawImage(scaledContext, CGRectMake(0.0, 0.0, scaledW.toDouble(), scaledH.toDouble()), cgImage)
        val scaledImage = CGBitmapContextCreateImage(scaledContext)
            ?: throw IllegalStateException("Failed to create scaled image")
        CGContextRelease(scaledContext)

        // Center crop to INPUT_SIZE × INPUT_SIZE
        val cropX = (scaledW - INPUT_SIZE) / 2
        val cropY = (scaledH - INPUT_SIZE) / 2
        val cropBytesPerRow = INPUT_SIZE * 4
        val cropContext = CGBitmapContextCreate(
            data = null,
            width = INPUT_SIZE.toULong(),
            height = INPUT_SIZE.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = cropBytesPerRow.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value
        ) ?: throw IllegalStateException("Failed to create crop bitmap context")

        // Draw the scaled image offset so the center lands at (0,0)
        CGContextDrawImage(
            cropContext,
            CGRectMake(-cropX.toDouble(), -cropY.toDouble(), scaledW.toDouble(), scaledH.toDouble()),
            scaledImage
        )

        val croppedImage = CGBitmapContextCreateImage(cropContext)
            ?: throw IllegalStateException("Failed to create cropped image")

        // Extract pixel bytes
        val dataProvider = CGImageGetDataProvider(croppedImage)
        val cfData = CGDataProviderCopyData(dataProvider)
            ?: throw IllegalStateException("Failed to copy pixel data")

        val length = CFDataGetLength(cfData).toInt()
        val bytePtr = CFDataGetBytePtr(cfData)
            ?: throw IllegalStateException("Failed to get pixel byte pointer")

        val result = ByteArray(length)
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytePtr, length.toULong())
        }

        // Cleanup
        CFRelease(cfData)
        CGImageRelease(croppedImage)
        CGContextRelease(cropContext)
        CGImageRelease(scaledImage)
        CGColorSpaceRelease(colorSpace)

        return result
    }

    private fun extractNormalizedTensor(rgbaBytes: ByteArray): ImageTensor {
        val pixelCount = INPUT_SIZE * INPUT_SIZE
        val data = FloatArray(3 * pixelCount)

        for (i in 0 until pixelCount) {
            val offset = i * 4
            val r = (rgbaBytes[offset].toInt() and 0xFF) / 255f
            val g = (rgbaBytes[offset + 1].toInt() and 0xFF) / 255f
            val b = (rgbaBytes[offset + 2].toInt() and 0xFF) / 255f

            data[i] = (r - MEAN_R) / STD_R
            data[pixelCount + i] = (g - MEAN_G) / STD_G
            data[2 * pixelCount + i] = (b - MEAN_B) / STD_B
        }

        return ImageTensor(data, TENSOR_SHAPE)
    }

    companion object {
        private const val INPUT_SIZE = 224
        private const val RESIZE_SIZE = 256

        private const val MEAN_R = 0.485f
        private const val MEAN_G = 0.456f
        private const val MEAN_B = 0.406f
        private const val STD_R = 0.229f
        private const val STD_G = 0.224f
        private const val STD_B = 0.225f

        private val TENSOR_SHAPE = intArrayOf(1, 3, INPUT_SIZE, INPUT_SIZE)
    }
}
