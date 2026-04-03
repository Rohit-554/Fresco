package io.jadu.fresco.domain.ml

import io.jadu.fresco.platform.ml.ClassificationOutput
import io.jadu.fresco.platform.ml.FruitClassifier
import io.jadu.fresco.platform.preprocessing.ImageTensor

class ClassifyImageUseCase(private val classifier: FruitClassifier) {
    suspend operator fun invoke(tensor: ImageTensor): ClassificationOutput =
        classifier.classify(tensor)
}
