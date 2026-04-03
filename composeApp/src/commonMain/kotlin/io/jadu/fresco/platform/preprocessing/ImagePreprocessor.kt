package io.jadu.fresco.platform.preprocessing

import io.jadu.fresco.platform.camera.CameraImage

/**
 * Decodes, resizes, and normalizes a captured camera image into an [ImageTensor]
 * suitable for EfficientNet-B0 inference (224×224, ImageNet normalization, NCHW layout).
 */
interface ImagePreprocessor {
    suspend fun preprocess(image: CameraImage): ImageTensor
}
