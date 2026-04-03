package io.jadu.fresco.di

import io.jadu.fresco.domain.camera.CaptureImageUseCase
import io.jadu.fresco.viewmodel.CameraViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::CaptureImageUseCase)
    viewModelOf(::CameraViewModel)
}
