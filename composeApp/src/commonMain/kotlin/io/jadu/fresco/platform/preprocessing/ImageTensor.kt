package io.jadu.fresco.platform.preprocessing

/**
 * Preprocessed image tensor ready for EfficientNet-B0 inference.
 *
 * @param data pixel values normalized with ImageNet mean/std in NCHW layout
 * @param shape tensor dimensions — `[1, 3, 224, 224]` for a single RGB image
 */
data class ImageTensor(val data: FloatArray, val shape: IntArray) {
    override fun equals(other: Any?) =
        other is ImageTensor && data.contentEquals(other.data) && shape.contentEquals(other.shape)

    override fun hashCode() = 31 * data.contentHashCode() + shape.contentHashCode()
}
