package io.jadu.fresco.domain.classification

import io.jadu.fresco.platform.ml.ClassificationOutput

/**
 * Extracts top-K [ClassificationResult]s from raw model output,
 * filtered by a minimum confidence threshold.
 */
class ResultInterpreter(
    private val topK: Int = DEFAULT_TOP_K,
    private val minConfidence: Float = DEFAULT_MIN_CONFIDENCE
) {

    fun interpret(output: ClassificationOutput): List<ClassificationResult> {
        val probabilities = output.probabilities

        // Get indices sorted by probability descending, take top-K
        return probabilities.indices
            .sortedByDescending { probabilities[it] }
            .take(topK)
            .filter { probabilities[it] >= minConfidence }
            .map { index ->
                ClassificationResult(
                    classIndex = index,
                    label = FruitVegLabels.labelFor(index),
                    confidence = probabilities[index],
                    isFruitOrVegetable = FruitVegLabels.isFruitOrVegetable(index)
                )
            }
    }

    companion object {
        const val DEFAULT_TOP_K = 5
        const val DEFAULT_MIN_CONFIDENCE = 0.01f
    }
}
