package com.d4rk.qrcodescanner.plus.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.main.BottomNavigationLabelsPreference
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferences
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.main.StartDestinationPreference
import com.d4rk.qrcodescanner.plus.domain.main.ThemePreference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

class SharedPreferencesMainPreferencesRepository(
    context : Context ,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO
) : MainPreferencesRepository {

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

    private val trackedKeys = setOf(themeKey , languageKey , bottomNavigationLabelsKey , defaultTabKey)

    override val mainPreferences : Flow<MainPreferences> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _ , key ->
            if (key == null || trackedKeys.contains(key)) {
                trySend(readMainPreferences())
            }
        }

        trySend(readMainPreferences())

        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .flowOn(ioDispatcher)
        .conflate()
        .distinctUntilChanged()

    private fun readMainPreferences() : MainPreferences {
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
