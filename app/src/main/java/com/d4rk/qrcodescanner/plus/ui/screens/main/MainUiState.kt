package com.d4rk.qrcodescanner.plus.ui.screens.main

import androidx.appcompat.app.AppCompatDelegate
import com.d4rk.qrcodescanner.plus.R
import com.google.android.material.navigation.NavigationBarView

/**
 * Represents the state required to configure the navigation chrome of [MainActivity].
 */
data class MainUiState(
    @param:NavigationBarView.LabelVisibility val bottomNavVisibility: Int = NavigationBarView.LABEL_VISIBILITY_AUTO,
    val defaultNavDestination: Int = R.id.navigation_scan,
    val themeMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    val languageTag: String? = null,
    val themeChanged: Boolean = false
)
