package io.jadu.fresco.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume

@Composable
actual fun RequestCameraPermission(
    onGranted: () -> Unit,
    onDenied: (isPermanent: Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        val granted = requestIosCameraPermission()
        if (granted) {
            onGranted()
        } else {
            onDenied(isPermissionPermanentlyDenied())
        }
    }
}

private suspend fun requestIosCameraPermission(): Boolean = suspendCancellableCoroutine { cont ->
    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted: Boolean ->
        cont.resume(granted)
    }
}

private fun isPermissionPermanentlyDenied(): Boolean {
    val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    return status == AVAuthorizationStatusDenied || status == AVAuthorizationStatusRestricted
}
