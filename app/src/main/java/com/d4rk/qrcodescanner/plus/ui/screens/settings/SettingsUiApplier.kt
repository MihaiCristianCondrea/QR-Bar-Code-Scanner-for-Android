package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.d4rk.qrcodescanner.plus.R

object SettingsUiApplier {
    fun apply(activity: AppCompatActivity, uiState: SettingsUiState) {
        AppCompatDelegate.setDefaultNightMode(uiState.themeMode)
        val languageTag = uiState.languageTag ?: activity.getString(R.string.default_value_language)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
