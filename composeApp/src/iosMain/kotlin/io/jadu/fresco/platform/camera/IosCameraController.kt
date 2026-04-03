package io.jadu.fresco.platform.camera

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.fileDataRepresentation
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
class IosCameraController : CameraController {

    val captureSession = AVCaptureSession().also { session ->
        session.sessionPreset = AVCaptureSessionPresetPhoto
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return@also
        val input = memScoped {
            val error = alloc<kotlinx.cinterop.ObjCObjectVar<NSError?>>()
            runCatching { AVCaptureDeviceInput(device = device, error = error.ptr) }.getOrNull()
        } ?: return@also
        if (session.canAddInput(input)) session.addInput(input)
    }

    private val photoOutput = AVCapturePhotoOutput().also { output ->
        if (captureSession.canAddOutput(output)) captureSession.addOutput(output)
    }

    override suspend fun captureImage(): CameraImage = suspendCancellableCoroutine { cont ->
        val settings = AVCapturePhotoSettings.photoSettingsWithFormat(
            mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG)
        )
        photoOutput.capturePhotoWithSettings(
            settings,
            delegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
                override fun captureOutput(
                    output: AVCapturePhotoOutput,
                    didFinishProcessingPhoto: AVCapturePhoto,
                    error: NSError?
                ) {
                    if (error != null) {
                        cont.resumeWithException(RuntimeException(error.localizedDescription))
                        return
                    }
                    val data = didFinishProcessingPhoto.fileDataRepresentation()
                    if (data == null) {
                        cont.resumeWithException(RuntimeException("No image data"))
                        return
                    }
                    cont.resume(CameraImage(data.toByteArray()))
                }
            }
        )
    }

    override fun release() {
        captureSession.stopRunning()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun platform.Foundation.NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return result
}
