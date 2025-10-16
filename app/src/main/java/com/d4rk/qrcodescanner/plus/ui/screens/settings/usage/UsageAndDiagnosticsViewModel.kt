package com.d4rk.qrcodescanner.plus.ui.screens.settings.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferences
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UsageAndDiagnosticsUiState(
    val usageAndDiagnosticsEnabled: Boolean = false,
    val analyticsConsentGranted: Boolean = false,
    val adStorageConsentGranted: Boolean = false,
    val adUserDataConsentGranted: Boolean = false,
    val adPersonalizationConsentGranted: Boolean = false,
)

class UsageAndDiagnosticsViewModel(
    private val repository: UsageAndDiagnosticsPreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    val uiState: StateFlow<UsageAndDiagnosticsUiState> = repository.observePreferences()
        .map { preferences -> preferences.toUiState() }
        .catch { throwable ->
            if (throwable is CancellationException) {
                throw throwable
            }
            emit(UsageAndDiagnosticsUiState())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UsageAndDiagnosticsUiState(),
        )

    fun setUsageAndDiagnostics(enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.setUsageAndDiagnostics(enabled)
        }
    }

    fun setAnalyticsConsent(granted: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.setAnalyticsConsent(granted)
        }
    }

    fun setAdStorageConsent(granted: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.setAdStorageConsent(granted)
        }
    }

    fun setAdUserDataConsent(granted: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.setAdUserDataConsent(granted)
        }
    }

    fun setAdPersonalizationConsent(granted: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.setAdPersonalizationConsent(granted)
        }
    }

    private fun UsageAndDiagnosticsPreferences.toUiState(): UsageAndDiagnosticsUiState {
        return UsageAndDiagnosticsUiState(
            usageAndDiagnosticsEnabled = usageAndDiagnosticsEnabled,
            analyticsConsentGranted = analyticsConsentGranted,
            adStorageConsentGranted = adStorageConsentGranted,
            adUserDataConsentGranted = adUserDataConsentGranted,
            adPersonalizationConsentGranted = adPersonalizationConsentGranted,
        )
    }
}

class UsageAndDiagnosticsViewModelFactory(
    private val repository: UsageAndDiagnosticsPreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageAndDiagnosticsViewModel::class.java)) {
            return UsageAndDiagnosticsViewModel(repository, ioDispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
