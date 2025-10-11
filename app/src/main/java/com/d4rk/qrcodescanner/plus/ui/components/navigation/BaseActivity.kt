package com.d4rk.qrcodescanner.plus.ui.components.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder

abstract class BaseActivity : AppCompatActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("RestrictedApi")
    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu is MenuBuilder) {
            @Suppress("UsePropertyAccessSyntax")
            menu.setOptionalIconsVisible(true)
        }
        return super.onMenuOpened(featureId, menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}