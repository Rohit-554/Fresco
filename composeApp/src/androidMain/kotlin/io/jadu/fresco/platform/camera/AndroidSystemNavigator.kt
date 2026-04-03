package io.jadu.fresco.platform.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class AndroidSystemNavigator(private val context: Context) : SystemNavigator {

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
