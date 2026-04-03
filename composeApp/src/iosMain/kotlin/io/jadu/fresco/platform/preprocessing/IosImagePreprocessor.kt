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
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

/**
 * iOS implementation: decodes JPEG via [UIImage]/[CGImage], resizes to 224×224
 * using a bitmap context, extracts RGB pixels, and normalizes with ImageNet
 * mean/std into NCHW layout.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosImagePreprocessor : ImagePreprocessor {

    override suspend fun preprocess(image: CameraImage): ImageTensor = withContext(Dispatchers.IO) {
        val nsData = image.bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = image.bytes.size.toULong())
        }

        val uiImage = UIImage.imageWithData(nsData)
            ?: throw IllegalArgumentException("Failed to decode image bytes")
        val cgImage = uiImage.CGImage
            ?: throw IllegalArgumentException("Failed to get CGImage")

        val resizedData = resizeToRGBA(cgImage)
        extractNormalizedTensor(resizedData)
    }

    /**
     * Draws [cgImage] into a 224×224 RGBA bitmap context and returns the raw pixel bytes.
     */
    private fun resizeToRGBA(cgImage: platform.CoreGraphics.CGImageRef): ByteArray {
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        // 4 bytes per pixel: RGBA (kCGImageAlphaNoneSkipLast = RGB + skip alpha)
        val bytesPerRow = INPUT_SIZE * 4
        val context = CGBitmapContextCreate(
            data = null,
            width = INPUT_SIZE.toULong(),
            height = INPUT_SIZE.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = bytesPerRow.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value
        ) ?: throw IllegalStateException("Failed to create bitmap context")

        CGContextDrawImage(context, CGRectMake(0.0, 0.0, INPUT_SIZE.toDouble(), INPUT_SIZE.toDouble()), cgImage)
        val outputImage = CGBitmapContextCreateImage(context)
            ?: throw IllegalStateException("Failed to create resized image")

        val dataProvider = CGImageGetDataProvider(outputImage)
        val cfData = CGDataProviderCopyData(dataProvider)
            ?: throw IllegalStateException("Failed to copy pixel data")

        val length = CFDataGetLength(cfData).toInt()
        val bytePtr = CFDataGetBytePtr(cfData)
            ?: throw IllegalStateException("Failed to get pixel byte pointer")

        val result = ByteArray(length)
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytePtr, length.toULong())
        }

        CFRelease(cfData)
        CGImageRelease(outputImage)
        CGContextRelease(context)
        CGColorSpaceRelease(colorSpace)

        return result
    }

    /**
     * Converts raw RGBA pixel bytes (224×224×4) into a normalized NCHW float tensor.
     */
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

        private const val MEAN_R = 0.485f
        private const val MEAN_G = 0.456f
        private const val MEAN_B = 0.406f
        private const val STD_R = 0.229f
        private const val STD_G = 0.224f
        private const val STD_B = 0.225f

        private val TENSOR_SHAPE = intArrayOf(1, 3, INPUT_SIZE, INPUT_SIZE)
    }
}
