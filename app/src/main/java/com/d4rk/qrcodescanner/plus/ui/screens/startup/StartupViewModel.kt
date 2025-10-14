package com.d4rk.qrcodescanner.plus.ui.screens.startup

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.data.startup.ConsentRequestOutcome
import com.d4rk.qrcodescanner.plus.data.startup.StartupConsentRepository
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StartupUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface StartupUiEvent {
    object NavigateToOnboarding : StartupUiEvent
    data class ShowConsentForm(val consentForm: ConsentForm) : StartupUiEvent
}

class StartupViewModel(
    private val consentRepository: StartupConsentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StartupUiState())
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StartupUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<StartupUiEvent> = _events.asSharedFlow()

    private var consentRequestJob: Job? = null

    fun initialize(activity: Activity) {
        if (consentRequestJob?.isActive == true) return
        requestConsent(activity)
    }

    fun onConsentFormDismissed(activity: Activity) {
        requestConsent(activity, force = true)
    }

    fun onAgreeClicked() {
        viewModelScope.launch {
            emitNavigation()
        }
    }

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }

    private fun requestConsent(activity: Activity, force: Boolean = false) {
        if (!force && consentRequestJob?.isActive == true) return
        consentRequestJob?.cancel()
        consentRequestJob = viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }

            val outcome = runCatching { consentRepository.requestConsent(activity) }
                .getOrElse { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: throwable.localizedMessage,
                        )
                    }
                    return@launch
                }

            when (outcome) {
                is ConsentRequestOutcome.Success -> handleConsentSuccess(activity, outcome)
                is ConsentRequestOutcome.Failure -> handleConsentFailure(outcome.message)
            }
        }
    }

    private suspend fun handleConsentSuccess(
        activity: Activity,
        outcome: ConsentRequestOutcome.Success,
    ) {
        when (outcome.consentStatus) {
            ConsentInformation.ConsentStatus.REQUIRED -> {
                if (outcome.isConsentFormAvailable) {
                    loadConsentForm(activity)
                } else {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "Consent form is required but not available.",
                        )
                    }
                }
            }

            ConsentInformation.ConsentStatus.OBTAINED,
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> {
                _uiState.update { current -> current.copy(isLoading = false) }
                emitNavigation()
            }

            else -> {
                _uiState.update { current -> current.copy(isLoading = false) }
            }
        }
    }

    private fun handleConsentFailure(message: String?) {
        _uiState.update { current ->
            current.copy(
                isLoading = false,
                errorMessage = message ?: "Unable to request consent at this time.",
            )
        }
    }

    private suspend fun loadConsentForm(activity: Activity) {
        val consentForm = runCatching { consentRepository.loadConsentForm(activity) }
            .getOrElse { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: throwable.localizedMessage,
                    )
                }
                return
            }

        _uiState.update { current -> current.copy(isLoading = false) }
        _events.emit(StartupUiEvent.ShowConsentForm(consentForm))
    }

    private suspend fun emitNavigation() {
        _events.emit(StartupUiEvent.NavigateToOnboarding)
    }
}

class StartupViewModelFactory(
    private val consentRepository: StartupConsentRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartupViewModel::class.java)) {
            return StartupViewModel(consentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${'$'}modelClass")
    }
}
