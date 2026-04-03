package io.jadu.fresco.platform.preprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import io.jadu.fresco.platform.camera.CameraImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation: decodes JPEG, applies rotation, resizes shorter side to 256,
 * center-crops to 224×224, and normalizes with ImageNet mean/std into NCHW layout.
 *
 * This matches the torchvision EfficientNet-B0 preprocessing:
 * Resize(256) → CenterCrop(224) → ToTensor → Normalize(mean, std)
 */
class AndroidImagePreprocessor : ImagePreprocessor {

    override suspend fun preprocess(image: CameraImage): ImageTensor = withContext(Dispatchers.IO) {
        var bitmap = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
            ?: throw IllegalArgumentException("Failed to decode image bytes")

        // Apply rotation from camera sensor
        if (image.rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(image.rotationDegrees.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            bitmap = rotated
        }

        // Resize shorter side to RESIZE_SIZE, maintaining aspect ratio
        val (w, h) = bitmap.width to bitmap.height
        val scale = RESIZE_SIZE.toFloat() / minOf(w, h)
        val scaledW = (w * scale).toInt()
        val scaledH = (h * scale).toInt()
        val resized = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)
        if (resized !== bitmap) bitmap.recycle()

        // Center crop to INPUT_SIZE × INPUT_SIZE
        val cropX = (resized.width - INPUT_SIZE) / 2
        val cropY = (resized.height - INPUT_SIZE) / 2
        val cropped = Bitmap.createBitmap(resized, cropX, cropY, INPUT_SIZE, INPUT_SIZE)
        if (cropped !== resized) resized.recycle()

        val tensor = extractNormalizedTensor(cropped)
        cropped.recycle()
        tensor
    }

    private fun extractNormalizedTensor(bitmap: Bitmap): ImageTensor {
        val pixelCount = INPUT_SIZE * INPUT_SIZE
        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        // NCHW layout: [1, 3, 224, 224]
        val data = FloatArray(3 * pixelCount)

        for (i in 0 until pixelCount) {
            val pixel = pixels[i]
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

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
