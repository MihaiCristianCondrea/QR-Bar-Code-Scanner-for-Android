package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferences
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.main.ThemePreference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SettingsUiState(
    val themeMode : Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ,
    val languageTag : String? = null
)

class SettingsViewModel(
    mainPreferencesRepository : MainPreferencesRepository ,
    computationDispatcher : CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    val uiState : StateFlow<SettingsUiState> = mainPreferencesRepository.mainPreferences
        .map { preferences -> preferences.toUiState() }
        .flowOn(computationDispatcher)
        .catch { throwable ->
            if (throwable is CancellationException) {
                throw throwable
            }
            emit(SettingsUiState())
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope ,
            started = SharingStarted.WhileSubscribed(5_000) ,
            initialValue = SettingsUiState()
        )

    private fun MainPreferences.toUiState() : SettingsUiState {
        return SettingsUiState(
            themeMode = theme.toThemeMode() ,
            languageTag = languageTag
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
}

class SettingsViewModelFactory(
    private val mainPreferencesRepository : MainPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass : Class<T>) : T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(mainPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
