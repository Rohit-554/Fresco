package io.jadu.fresco.viewmodel

import io.jadu.fresco.domain.classification.ClassificationResult
import io.jadu.fresco.platform.camera.CameraImage

sealed interface CameraUiState {
    data object PermissionPrimer : CameraUiState
    data object RequestingPermission : CameraUiState
    data object PermanentlyDenied : CameraUiState
    data object Preview : CameraUiState
    data object Processing : CameraUiState
    data object Classifying : CameraUiState
    data class Classified(
        val image: CameraImage,
        val results: List<ClassificationResult>
    ) : CameraUiState
    data class Error(val message: String) : CameraUiState
}
