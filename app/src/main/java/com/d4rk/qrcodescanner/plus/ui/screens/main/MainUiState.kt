package com.d4rk.qrcodescanner.plus.ui.screens.main

import com.google.android.material.navigation.NavigationBarView

/**
 * Represents the state required to configure the navigation chrome of [MainActivity].
 */
data class MainUiState(
    @NavigationBarView.LabelVisibility val bottomNavVisibility : Int , val defaultNavDestination : Int , val themeMode : Int , val languageTag : String? , val themeChanged : Boolean
)
