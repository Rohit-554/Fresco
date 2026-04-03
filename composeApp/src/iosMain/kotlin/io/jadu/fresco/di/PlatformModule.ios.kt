package io.jadu.fresco.di

import io.jadu.fresco.platform.camera.CameraController
import io.jadu.fresco.platform.camera.CameraPermission
import io.jadu.fresco.platform.camera.IosCameraController
import io.jadu.fresco.platform.camera.IosCameraPermission
import io.jadu.fresco.platform.camera.IosSystemNavigator
import io.jadu.fresco.platform.camera.SystemNavigator
import io.jadu.fresco.platform.ml.FruitClassifier
import io.jadu.fresco.platform.ml.IosFruitClassifier
import io.jadu.fresco.platform.preprocessing.ImagePreprocessor
import io.jadu.fresco.platform.preprocessing.IosImagePreprocessor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { IosCameraController() }
    single<CameraController> { get<IosCameraController>() }
    single<CameraPermission> { IosCameraPermission() }
    single<SystemNavigator> { IosSystemNavigator() }
    single<ImagePreprocessor> { IosImagePreprocessor() }
    single<FruitClassifier> { IosFruitClassifier() }
}
