package io.jadu.fresco.domain.preprocessing

import io.jadu.fresco.platform.camera.CameraImage
import io.jadu.fresco.platform.preprocessing.ImagePreprocessor
import io.jadu.fresco.platform.preprocessing.ImageTensor

class PreprocessImageUseCase(private val imagePreprocessor: ImagePreprocessor) {
    suspend operator fun invoke(image: CameraImage): ImageTensor =
        imagePreprocessor.preprocess(image)
}
