package com.d4rk.qrcodescanner.plus.ui.components.navigation

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.setupToolbarWithUpNavigation() {
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
}
