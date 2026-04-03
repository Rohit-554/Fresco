package io.jadu.fresco.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.jadu.fresco.platform.camera.AndroidCameraController
import org.koin.compose.koinInject

@Composable
actual fun CameraPreview(modifier: Modifier) {
    val controller = koinInject<AndroidCameraController>()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                controller.bindToLifecycle(lifecycleOwner, previewView.surfaceProvider)
            }
        },
        modifier = modifier
    )
}
