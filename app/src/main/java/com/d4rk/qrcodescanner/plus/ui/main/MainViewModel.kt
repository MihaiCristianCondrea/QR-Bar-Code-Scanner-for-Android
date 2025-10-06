package com.d4rk.qrcodescanner.plus.ui.main

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R
import com.google.android.material.navigation.NavigationBarView

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val resources = application.resources

    private val _uiState = MutableLiveData<MainUiState>()
    val uiState: LiveData<MainUiState> = _uiState

    private var lastThemeMode: Int? = null
    private var lastLanguageTag: String? = null

    fun applySettings(
        themeValues: Array<String>,
        bottomNavBarLabelsValues: Array<String>,
        defaultTabValues: Array<String>
    ) {
        val themeMode = resolveThemeMode(themeValues)
        val languageTag = preferences.getString(
            resources.getString(R.string.key_language),
            resources.getString(R.string.default_value_language)
        )
        val labelVisibility = resolveBottomBarLabelVisibility(bottomNavBarLabelsValues)
        val startDestination = resolveStartDestination(defaultTabValues)

        val themeChanged = (lastThemeMode != null && lastThemeMode != themeMode) ||
            (lastLanguageTag != null && lastLanguageTag != languageTag)

        lastThemeMode = themeMode
        lastLanguageTag = languageTag

        _uiState.value = MainUiState(
            bottomNavVisibility = labelVisibility,
            defaultNavDestination = startDestination,
            themeMode = themeMode,
            languageTag = languageTag,
            themeChanged = themeChanged
        )
    }

    private fun resolveThemeMode(themeValues: Array<String>): Int {
        val themePreference = preferences.getString(
            resources.getString(R.string.key_theme),
            resources.getString(R.string.default_value_theme)
        )
        return when (themePreference) {
            themeValues.getOrNull(1) -> AppCompatDelegate.MODE_NIGHT_NO
            themeValues.getOrNull(2) -> AppCompatDelegate.MODE_NIGHT_YES
            themeValues.getOrNull(3) -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    private fun resolveBottomBarLabelVisibility(bottomNavBarLabelsValues: Array<String>): Int {
        val defaultValue = resources.getString(R.string.default_value_bottom_navigation_bar_labels)
        val labelPreference = preferences.getString(
            resources.getString(R.string.key_bottom_navigation_bar_labels),
            defaultValue
        )
        return when (labelPreference) {
            bottomNavBarLabelsValues.getOrNull(0) -> NavigationBarView.LABEL_VISIBILITY_LABELED
            bottomNavBarLabelsValues.getOrNull(1) -> NavigationBarView.LABEL_VISIBILITY_SELECTED
            bottomNavBarLabelsValues.getOrNull(2) -> NavigationBarView.LABEL_VISIBILITY_UNLABELED
            else -> NavigationBarView.LABEL_VISIBILITY_AUTO
        }
    }

    private fun resolveStartDestination(defaultTabValues: Array<String>): Int {
        val defaultValue = resources.getString(R.string.default_value_tab)
        val selectedTab = preferences.getString(
            resources.getString(R.string.key_default_tab),
            defaultValue
        )
        return when (selectedTab) {
            defaultTabValues.getOrNull(0) -> R.id.navigation_scan
            defaultTabValues.getOrNull(1) -> R.id.navigation_create
            defaultTabValues.getOrNull(2) -> R.id.navigation_history
            else -> R.id.navigation_scan
        }
    }
}
