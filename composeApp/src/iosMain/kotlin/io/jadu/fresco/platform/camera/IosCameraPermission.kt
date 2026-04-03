package io.jadu.fresco.platform.camera

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType

class IosCameraPermission : CameraPermission {

    override fun currentStatus(): PermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.Granted
            AVAuthorizationStatusDenied,
            AVAuthorizationStatusRestricted -> PermissionStatus.PermanentlyDenied
            else -> PermissionStatus.NotRequested
        }
    }
}
