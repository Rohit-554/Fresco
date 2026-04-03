package io.jadu.fresco.di

import io.jadu.fresco.platform.camera.AndroidCameraController
import io.jadu.fresco.platform.camera.AndroidCameraPermission
import io.jadu.fresco.platform.camera.AndroidSystemNavigator
import io.jadu.fresco.platform.camera.CameraController
import io.jadu.fresco.platform.camera.CameraPermission
import io.jadu.fresco.platform.camera.SystemNavigator
import io.jadu.fresco.platform.preprocessing.AndroidImagePreprocessor
import io.jadu.fresco.platform.preprocessing.ImagePreprocessor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { AndroidCameraController(androidContext()) }
    single<CameraController> { get<AndroidCameraController>() }
    single<CameraPermission> { AndroidCameraPermission(androidContext()) }
    single<SystemNavigator> { AndroidSystemNavigator(androidContext()) }
    single<ImagePreprocessor> { AndroidImagePreprocessor() }
}
