package com.d4rk.qrcodescanner.plus.ui.components.navigation

abstract class UpNavigationActivity : BaseActivity() {
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
