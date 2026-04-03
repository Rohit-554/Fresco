package io.jadu.fresco.platform.camera

interface CameraController {
    suspend fun captureImage(): CameraImage
    fun release()
}
