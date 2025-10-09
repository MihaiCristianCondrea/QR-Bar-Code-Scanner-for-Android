package com.d4rk.qrcodescanner.plus.data.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.main.BottomNavigationLabelsPreference
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferences
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.main.StartDestinationPreference
import com.d4rk.qrcodescanner.plus.domain.main.ThemePreference

class SharedPreferencesMainPreferencesRepository(context : Context) : MainPreferencesRepository {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources

    private val themeKey = resources.getString(R.string.key_theme)
    private val languageKey = resources.getString(R.string.key_language)
    private val bottomNavigationLabelsKey = resources.getString(R.string.key_bottom_navigation_bar_labels)
    private val defaultTabKey = resources.getString(R.string.key_default_tab)

    private val defaultThemeValue = resources.getString(R.string.default_value_theme)
    private val defaultLanguageValue = resources.getString(R.string.default_value_language)
    private val defaultBottomNavigationLabelsValue = resources.getString(R.string.default_value_bottom_navigation_bar_labels)
    private val defaultStartDestinationValue = resources.getString(R.string.default_value_tab)

    override fun getMainPreferences() : MainPreferences {
        val themePreference = preferences.getString(themeKey , defaultThemeValue)
        val languageTag = preferences.getString(languageKey , defaultLanguageValue) ?: defaultLanguageValue
        val labelsPreference = preferences.getString(bottomNavigationLabelsKey , defaultBottomNavigationLabelsValue)
        val startDestinationPreference = preferences.getString(defaultTabKey , defaultStartDestinationValue)

        return MainPreferences(
            theme = when (themePreference) {
                "MODE_NIGHT_NO" -> ThemePreference.LIGHT
                "MODE_NIGHT_YES" -> ThemePreference.DARK
                "MODE_NIGHT_AUTO_BATTERY" -> ThemePreference.AUTO_BATTERY
                else -> ThemePreference.FOLLOW_SYSTEM
            } ,
            languageTag = languageTag ,
            bottomNavigationLabels = when (labelsPreference) {
                "selected" -> BottomNavigationLabelsPreference.SELECTED
                "unlabeled" -> BottomNavigationLabelsPreference.UNLABELED
                else -> BottomNavigationLabelsPreference.LABELED
            } ,
            startDestination = when (startDestinationPreference) {
                "create" -> StartDestinationPreference.CREATE
                "history" -> StartDestinationPreference.HISTORY
                else -> StartDestinationPreference.SCAN
            }
        )
    }
}
