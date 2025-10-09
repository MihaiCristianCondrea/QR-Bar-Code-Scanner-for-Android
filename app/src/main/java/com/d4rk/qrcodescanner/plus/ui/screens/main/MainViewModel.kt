package com.d4rk.qrcodescanner.plus.ui.screens.main

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.main.BottomNavigationLabelsPreference
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.main.StartDestinationPreference
import com.d4rk.qrcodescanner.plus.domain.main.ThemePreference
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val preferencesRepository : MainPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState : StateFlow<MainUiState> = _uiState.asStateFlow()

    private var lastThemeMode : Int? = null
    private var lastLanguageTag : String? = null

    fun refreshSettings() {
        val preferences = preferencesRepository.getMainPreferences()

        val themeMode = preferences.theme.toThemeMode()
        val languageTag = preferences.languageTag
        val labelVisibility = preferences.bottomNavigationLabels.toLabelVisibility()
        val startDestination = preferences.startDestination.toStartDestinationId()

        val themeChanged = (lastThemeMode != null && lastThemeMode != themeMode) || (lastLanguageTag != null && lastLanguageTag != languageTag)

        lastThemeMode = themeMode
        lastLanguageTag = languageTag

        _uiState.value = MainUiState(
            bottomNavVisibility = labelVisibility ,
            defaultNavDestination = startDestination ,
            themeMode = themeMode ,
            languageTag = languageTag ,
            themeChanged = themeChanged
        )
    }

    private fun ThemePreference.toThemeMode() : Int {
        return when (this) {
            ThemePreference.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemePreference.AUTO_BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }

    private fun BottomNavigationLabelsPreference.toLabelVisibility() : Int {
        return when (this) {
            BottomNavigationLabelsPreference.LABELED -> NavigationBarView.LABEL_VISIBILITY_LABELED
            BottomNavigationLabelsPreference.SELECTED -> NavigationBarView.LABEL_VISIBILITY_SELECTED
            BottomNavigationLabelsPreference.UNLABELED -> NavigationBarView.LABEL_VISIBILITY_UNLABELED
        }
    }

    private fun StartDestinationPreference.toStartDestinationId() : Int {
        return when (this) {
            StartDestinationPreference.SCAN -> R.id.navigation_scan
            StartDestinationPreference.CREATE -> R.id.navigation_create
            StartDestinationPreference.HISTORY -> R.id.navigation_history
        }
    }
}
