package io.jadu.fresco.platform.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class AndroidCameraPermission(private val context: Context) : CameraPermission {

    override fun currentStatus(): PermissionStatus {
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.NotRequested
        }
    }
}
