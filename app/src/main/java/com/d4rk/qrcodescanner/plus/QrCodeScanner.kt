package com.d4rk.qrcodescanner.plus

import android.app.Application
import com.d4rk.qrcodescanner.plus.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class QrCodeScanner : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@QrCodeScanner)
            modules(appModule)
        }
    }
}
