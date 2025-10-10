package com.d4rk.qrcodescanner.plus.ui.components.navigation

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

fun AppCompatActivity.setupToolbarWithUpNavigation(toolbar: MaterialToolbar) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
}
