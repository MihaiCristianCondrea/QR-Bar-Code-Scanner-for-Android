package com.d4rk.qrcodescanner.plus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QrCodeScanner : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: QrCodeScanner
            private set
    }
}
