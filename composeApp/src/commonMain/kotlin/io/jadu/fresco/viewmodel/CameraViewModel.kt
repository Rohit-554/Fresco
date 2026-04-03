package io.jadu.fresco.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.fresco.domain.camera.CaptureImageUseCase
import io.jadu.fresco.domain.preprocessing.PreprocessImageUseCase
import io.jadu.fresco.platform.camera.CameraPermission
import io.jadu.fresco.platform.camera.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    private val cameraPermission: CameraPermission,
    private val captureImageUseCase: CaptureImageUseCase,
    private val preprocessImageUseCase: PreprocessImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(determineInitialState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private fun determineInitialState(): CameraUiState = when (cameraPermission.currentStatus()) {
        PermissionStatus.Granted -> CameraUiState.Preview
        PermissionStatus.PermanentlyDenied -> CameraUiState.PermanentlyDenied
        else -> CameraUiState.PermissionPrimer
    }

    fun onPermissionPrimerAccepted() {
        _uiState.value = CameraUiState.RequestingPermission
    }

    fun onPermissionPrimerDismissed() {
        // User chose not now — app stays usable without camera
    }

    fun onPermissionGranted() {
        _uiState.value = CameraUiState.Preview
    }

    fun onPermissionDenied(isPermanent: Boolean) {
        _uiState.value = if (isPermanent) {
            CameraUiState.PermanentlyDenied
        } else {
            CameraUiState.PermissionPrimer
        }
    }

    fun onCaptureRequested() {
        viewModelScope.launch { captureImage() }
    }

    fun onRetry() {
        _uiState.value = CameraUiState.PermissionPrimer
    }

    private suspend fun captureImage() {
        // Capture while Preview composable is still mounted (camera bound)
        val image = runCatching { captureImageUseCase() }
            .getOrElse {
                _uiState.value = CameraUiState.Error(it.message ?: "Capture failed")
                return
            }
        // Now safe to leave Preview — camera can be released
        _uiState.value = CameraUiState.Processing
        _uiState.value = runCatching { preprocessImageUseCase(image) }
            .fold(
                onSuccess = { tensor -> CameraUiState.Captured(image, tensor) },
                onFailure = { CameraUiState.Error(it.message ?: "Processing failed") }
            )
    }
}
