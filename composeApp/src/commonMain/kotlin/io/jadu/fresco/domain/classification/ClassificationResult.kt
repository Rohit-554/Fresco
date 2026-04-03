package io.jadu.fresco.domain.classification

/**
 * A single classification prediction with human-readable label.
 *
 * @param classIndex raw model output index (0–999 for ImageNet)
 * @param label human-readable class name (e.g. "banana", "strawberry")
 * @param confidence probability in [0, 1]
 * @param isFruitOrVegetable true if this class belongs to the fruit/vegetable subset
 */
data class ClassificationResult(
    val classIndex: Int,
    val label: String,
    val confidence: Float,
    val isFruitOrVegetable: Boolean
)
