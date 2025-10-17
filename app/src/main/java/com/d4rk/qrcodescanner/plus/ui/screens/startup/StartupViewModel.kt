package com.d4rk.qrcodescanner.plus.ui.screens.startup

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.data.startup.StartupConsentRepository
import com.d4rk.qrcodescanner.plus.ui.consent.ConsentFlowCoordinator
import com.d4rk.qrcodescanner.plus.ui.consent.ConsentFlowResult
import com.google.android.ump.ConsentForm
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

    private val consentCoordinator = ConsentFlowCoordinator(consentRepository)

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
        val job = viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }

            val result = runCatching { consentCoordinator.prepareConsentUi(activity) }
                .getOrElse { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: throwable.localizedMessage,
                        )
                    }
                    return@launch
                }

            when (result) {
                ConsentFlowResult.ConsentSatisfied -> {
                    _uiState.update { current -> current.copy(isLoading = false) }
                    emitNavigation()
                }

                is ConsentFlowResult.ShowConsentForm -> handleConsentForm(result)
                is ConsentFlowResult.Error -> handleConsentFailure(result.message)
                ConsentFlowResult.ShowPrivacyOptionsForm -> {
                    _uiState.update { current -> current.copy(isLoading = false) }
                    emitNavigation()
                }
            }
        }
        job.invokeOnCompletion { consentRequestJob = null }
        consentRequestJob = job
    }

    private fun handleConsentFailure(message: String?) {
        _uiState.update { current ->
            current.copy(
                isLoading = false,
                errorMessage = message ?: "Unable to request consent at this time.",
            )
        }
    }

    private suspend fun handleConsentForm(result: ConsentFlowResult.ShowConsentForm) {
        _uiState.update { current -> current.copy(isLoading = false) }
        _events.emit(StartupUiEvent.ShowConsentForm(result.consentForm))
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
