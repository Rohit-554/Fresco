package io.jadu.fresco.platform.ml

/**
 * Raw output from the EfficientNet-B0 model.
 *
 * @param probabilities softmax probabilities for each class, indexed by class ID
 */
data class ClassificationOutput(val probabilities: FloatArray) {

    val predictedClassIndex: Int get() = probabilities.indices.maxBy { probabilities[it] }
    val confidence: Float get() = probabilities[predictedClassIndex]

    override fun equals(other: Any?) =
        other is ClassificationOutput && probabilities.contentEquals(other.probabilities)

    override fun hashCode() = probabilities.contentHashCode()
}
