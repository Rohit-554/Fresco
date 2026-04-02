package io.jadu.fresco.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Platform-specific bindings (camera, classifier, storage) added per phase
}
