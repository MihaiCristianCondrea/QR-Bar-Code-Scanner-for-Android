package com.d4rk.qrcodescanner.plus.ui.screens.main

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.main.BottomNavigationLabelsPreference
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferences
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.main.StartDestinationPreference
import com.d4rk.qrcodescanner.plus.domain.main.ThemePreference
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    preferencesRepository: MainPreferencesRepository,
    computationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private data class UiAccumulator(
        val lastLanguageTag: String? = null,
        val uiState: MainUiState = MainUiState()
    )

    private data class PreferencesSnapshot(
        val themeMode: Int,
        val languageTag: String?,
        val labelVisibility: Int,
        val startDestination: Int
    )

    val uiState: StateFlow<MainUiState> = preferencesRepository.mainPreferences
        .map { preferences ->
            preferences.toSnapshot()
        }
        .runningFold(UiAccumulator()) { accumulator, snapshot ->
            val languageChanged = accumulator.lastLanguageTag != null &&
                    accumulator.lastLanguageTag != snapshot.languageTag

            UiAccumulator(
                lastLanguageTag = snapshot.languageTag,
                uiState = MainUiState(
                    bottomNavVisibility = snapshot.labelVisibility,
                    defaultNavDestination = snapshot.startDestination,
                    themeMode = snapshot.themeMode,
                    languageTag = snapshot.languageTag,
                    requiresRecreation = languageChanged
                )
            )
        }
        .drop(1)
        .map { accumulator -> accumulator.uiState }
        .flowOn(computationDispatcher)
        .catch { throwable ->
            if (throwable is CancellationException) {
                throw throwable
            }
            emit(MainUiState())
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    private fun ThemePreference.toThemeMode(): Int {
        return when (this) {
            ThemePreference.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemePreference.AUTO_BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }

    private fun BottomNavigationLabelsPreference.toLabelVisibility(): Int {
        return when (this) {
            BottomNavigationLabelsPreference.LABELED -> NavigationBarView.LABEL_VISIBILITY_LABELED
            BottomNavigationLabelsPreference.SELECTED -> NavigationBarView.LABEL_VISIBILITY_SELECTED
            BottomNavigationLabelsPreference.UNLABELED -> NavigationBarView.LABEL_VISIBILITY_UNLABELED
        }
    }

    private fun StartDestinationPreference.toStartDestinationId(): Int {
        return when (this) {
            StartDestinationPreference.SCAN -> R.id.navigation_scan
            StartDestinationPreference.CREATE -> R.id.navigation_create
            StartDestinationPreference.HISTORY -> R.id.navigation_history
        }
    }

    private fun MainPreferences.toSnapshot(): PreferencesSnapshot {
        return PreferencesSnapshot(
            themeMode = theme.toThemeMode(),
            languageTag = languageTag,
            labelVisibility = bottomNavigationLabels.toLabelVisibility(),
            startDestination = startDestination.toStartDestinationId()
        )
    }
}
