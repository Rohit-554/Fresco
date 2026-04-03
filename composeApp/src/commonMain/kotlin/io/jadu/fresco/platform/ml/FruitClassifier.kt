package io.jadu.fresco.platform.ml

import io.jadu.fresco.platform.preprocessing.ImageTensor

/**
 * Runs EfficientNet-B0 inference on a preprocessed [ImageTensor].
 *
 * Android: ONNX Runtime session.
 * iOS: Core ML MLModel.
 */
interface FruitClassifier {
    suspend fun classify(tensor: ImageTensor): ClassificationOutput
    fun close()
}
