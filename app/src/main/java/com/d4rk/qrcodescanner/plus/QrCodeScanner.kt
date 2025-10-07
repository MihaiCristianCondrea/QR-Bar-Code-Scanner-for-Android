package com.d4rk.qrcodescanner.plus

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QrCodeScanner : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: QrCodeScanner
            private set
    }
}
