package io.jadu.fresco.platform.preprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.jadu.fresco.platform.camera.CameraImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.scale

/**
 * Android implementation: decodes JPEG via [BitmapFactory], resizes to 224×224,
 * extracts RGB pixels, and normalizes with ImageNet mean/std into NCHW layout.
 */
class AndroidImagePreprocessor : ImagePreprocessor {

    override suspend fun preprocess(image: CameraImage): ImageTensor = withContext(Dispatchers.IO) {
        val original = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
            ?: throw IllegalArgumentException("Failed to decode image bytes")

        val resized = original.scale(INPUT_SIZE, INPUT_SIZE)
        if (resized !== original) original.recycle()

        val tensor = extractNormalizedTensor(resized)
        resized.recycle()
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

            data[i] = (r - MEAN_R) / STD_R                      // R channel
            data[pixelCount + i] = (g - MEAN_G) / STD_G         // G channel
            data[2 * pixelCount + i] = (b - MEAN_B) / STD_B     // B channel
        }

        return ImageTensor(data, TENSOR_SHAPE)
    }

    companion object {
        private const val INPUT_SIZE = 224

        // ImageNet normalization constants
        private const val MEAN_R = 0.485f
        private const val MEAN_G = 0.456f
        private const val MEAN_B = 0.406f
        private const val STD_R = 0.229f
        private const val STD_G = 0.224f
        private const val STD_B = 0.225f

        private val TENSOR_SHAPE = intArrayOf(1, 3, INPUT_SIZE, INPUT_SIZE)
    }
}
