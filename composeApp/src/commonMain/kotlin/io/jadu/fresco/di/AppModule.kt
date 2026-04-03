package io.jadu.fresco.di

import io.jadu.fresco.domain.camera.CaptureImageUseCase
import io.jadu.fresco.domain.classification.InterpretResultUseCase
import io.jadu.fresco.domain.classification.ResultInterpreter
import io.jadu.fresco.domain.ml.ClassifyImageUseCase
import io.jadu.fresco.domain.preprocessing.PreprocessImageUseCase
import io.jadu.fresco.viewmodel.CameraViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::CaptureImageUseCase)
    factoryOf(::PreprocessImageUseCase)
    factoryOf(::ClassifyImageUseCase)
    single { ResultInterpreter() }
    factoryOf(::InterpretResultUseCase)
    viewModelOf(::CameraViewModel)
}
