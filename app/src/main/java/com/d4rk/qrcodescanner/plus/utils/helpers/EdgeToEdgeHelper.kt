package com.d4rk.qrcodescanner.plus.utils.helpers

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

object EdgeToEdgeHelper {

    fun applyEdgeToEdge(window: Window, view: View) {
        WindowCompat.enableEdgeToEdge(window)
        applySystemBarsColor(window)
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    fun applySystemBarsColor(window: Window) {
        val isLightMode =
            (window.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = isLightMode
        controller.isAppearanceLightStatusBars = isLightMode
    }
}