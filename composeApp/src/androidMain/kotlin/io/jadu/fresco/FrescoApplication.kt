package io.jadu.fresco

import android.app.Application
import io.jadu.fresco.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class FrescoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FrescoApplication)
            androidLogger()
        }
    }
}
