package io.jadu.fresco.platform.camera

sealed interface PermissionStatus {
    data object NotRequested : PermissionStatus
    data object Granted : PermissionStatus
    data object Denied : PermissionStatus
    data object PermanentlyDenied : PermissionStatus
}
