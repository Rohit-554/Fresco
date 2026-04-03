package io.jadu.fresco

import io.jadu.fresco.domain.camera.CaptureImageUseCase
import io.jadu.fresco.platform.camera.CameraController
import io.jadu.fresco.platform.camera.CameraImage
import io.jadu.fresco.platform.camera.CameraPermission
import io.jadu.fresco.platform.camera.PermissionStatus
import io.jadu.fresco.viewmodel.CameraUiState
import io.jadu.fresco.viewmodel.CameraViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CameraViewModelTest {

    @Test
    fun `starts on PermissionPrimer when permission not yet requested`() {
        val viewModel = buildViewModel(status = PermissionStatus.NotRequested)
        assertIs<CameraUiState.PermissionPrimer>(viewModel.uiState.value)
    }

    @Test
    fun `starts on Preview when camera already granted`() {
        val viewModel = buildViewModel(status = PermissionStatus.Granted)
        assertIs<CameraUiState.Preview>(viewModel.uiState.value)
    }

    @Test
    fun `starts on PermanentlyDenied when previously blocked`() {
        val viewModel = buildViewModel(status = PermissionStatus.PermanentlyDenied)
        assertIs<CameraUiState.PermanentlyDenied>(viewModel.uiState.value)
    }

    @Test
    fun `accepting primer moves to RequestingPermission`() {
        val viewModel = buildViewModel(status = PermissionStatus.NotRequested)
        viewModel.onPermissionPrimerAccepted()
        assertIs<CameraUiState.RequestingPermission>(viewModel.uiState.value)
    }

    @Test
    fun `granting permission moves to Preview`() {
        val viewModel = buildViewModel(status = PermissionStatus.NotRequested)
        viewModel.onPermissionPrimerAccepted()
        viewModel.onPermissionGranted()
        assertIs<CameraUiState.Preview>(viewModel.uiState.value)
    }

    @Test
    fun `permanent denial moves to PermanentlyDenied`() {
        val viewModel = buildViewModel(status = PermissionStatus.NotRequested)
        viewModel.onPermissionPrimerAccepted()
        viewModel.onPermissionDenied(isPermanent = true)
        assertIs<CameraUiState.PermanentlyDenied>(viewModel.uiState.value)
    }

    @Test
    fun `soft denial returns to PermissionPrimer`() {
        val viewModel = buildViewModel(status = PermissionStatus.NotRequested)
        viewModel.onPermissionPrimerAccepted()
        viewModel.onPermissionDenied(isPermanent = false)
        assertIs<CameraUiState.PermissionPrimer>(viewModel.uiState.value)
    }

    @Test
    fun `successful capture moves to Captured`() = runTest {
        val image = CameraImage(byteArrayOf(1, 2, 3))
        val viewModel = buildViewModel(
            status = PermissionStatus.Granted,
            capturedImage = image
        )
        viewModel.onCaptureRequested()
        assertIs<CameraUiState.Captured>(viewModel.uiState.value)
        assertEquals(image, (viewModel.uiState.value as CameraUiState.Captured).image)
    }

    @Test
    fun `capture failure moves to Error`() = runTest {
        val viewModel = buildViewModel(
            status = PermissionStatus.Granted,
            captureError = RuntimeException("Camera failed")
        )
        viewModel.onCaptureRequested()
        assertIs<CameraUiState.Error>(viewModel.uiState.value)
        assertEquals("Camera failed", (viewModel.uiState.value as CameraUiState.Error).message)
    }

    private fun buildViewModel(
        status: PermissionStatus,
        capturedImage: CameraImage = CameraImage(byteArrayOf()),
        captureError: Throwable? = null
    ): CameraViewModel {
        val permission = object : CameraPermission {
            override fun currentStatus() = status
        }
        val controller = object : CameraController {
            override suspend fun captureImage(): CameraImage {
                if (captureError != null) throw captureError
                return capturedImage
            }
            override fun release() = Unit
        }
        return CameraViewModel(permission, CaptureImageUseCase(controller))
    }
}
