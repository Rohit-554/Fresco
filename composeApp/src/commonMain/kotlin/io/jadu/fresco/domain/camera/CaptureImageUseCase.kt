package io.jadu.fresco.domain.camera

import io.jadu.fresco.platform.camera.CameraController
import io.jadu.fresco.platform.camera.CameraImage

class CaptureImageUseCase(private val cameraController: CameraController) {
    suspend operator fun invoke(): CameraImage = cameraController.captureImage()
}
