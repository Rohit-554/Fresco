package io.jadu.fresco.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.jadu.fresco.platform.camera.IosCameraController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(modifier: Modifier) {
    val controller = koinInject<IosCameraController>()
    val previewLayer = remember {
        AVCaptureVideoPreviewLayer(session = controller.captureSession).also {
            it.videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    DisposableEffect(Unit) {
        controller.captureSession.startRunning()
        onDispose { controller.captureSession.stopRunning() }
    }

    UIKitView(
        factory = {
            PreviewContainerView(previewLayer)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PreviewContainerView(
    private val previewLayer: AVCaptureVideoPreviewLayer
) : UIView(frame = kotlinx.cinterop.cValue { }) {

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setValue(true, kCATransactionDisableActions)
        previewLayer.setFrame(bounds)
        CATransaction.commit()
    }

    init {
        layer.addSublayer(previewLayer)
    }
}
