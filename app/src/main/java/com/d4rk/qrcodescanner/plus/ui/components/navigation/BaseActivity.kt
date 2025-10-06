package com.d4rk.qrcodescanner.plus.ui.components.navigation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.qrcodescanner.plus.di.rotationHelper

abstract class BaseActivity : AppCompatActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        rotationHelper.lockCurrentOrientationIfNeeded(this)
    }
}