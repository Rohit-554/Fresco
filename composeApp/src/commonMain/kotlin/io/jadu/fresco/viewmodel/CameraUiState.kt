package io.jadu.fresco.viewmodel

import io.jadu.fresco.platform.camera.CameraImage
import io.jadu.fresco.platform.preprocessing.ImageTensor

sealed interface CameraUiState {
    data object PermissionPrimer : CameraUiState
    data object RequestingPermission : CameraUiState
    data object PermanentlyDenied : CameraUiState
    data object Preview : CameraUiState
    data object Processing : CameraUiState
    data class Captured(val image: CameraImage, val tensor: ImageTensor) : CameraUiState
    data class Error(val message: String) : CameraUiState
}
