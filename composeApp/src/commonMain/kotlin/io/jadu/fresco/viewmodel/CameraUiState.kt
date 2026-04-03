package io.jadu.fresco.viewmodel

import io.jadu.fresco.platform.camera.CameraImage

sealed interface CameraUiState {
    data object PermissionPrimer : CameraUiState
    data object RequestingPermission : CameraUiState
    data object PermanentlyDenied : CameraUiState
    data object Preview : CameraUiState
    data class Captured(val image: CameraImage) : CameraUiState
    data class Error(val message: String) : CameraUiState
}
