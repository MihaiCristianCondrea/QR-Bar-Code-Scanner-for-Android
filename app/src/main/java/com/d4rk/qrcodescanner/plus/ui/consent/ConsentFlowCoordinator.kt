package com.d4rk.qrcodescanner.plus.ui.consent

import android.app.Activity
import android.content.Context
import com.d4rk.qrcodescanner.plus.data.startup.ConsentRequestOutcome
import com.d4rk.qrcodescanner.plus.data.startup.StartupConsentRepository
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation

class ConsentFlowCoordinator(
    private val consentRepository: StartupConsentRepository,
) {

    constructor(context: Context) : this(StartupConsentRepository(context))

    suspend fun prepareConsentUi(
        activity: Activity,
        requestPrivacyOptionsOnSatisfied: Boolean = false,
    ): ConsentFlowResult {
        return when (val outcome = consentRepository.requestConsent(activity)) {
            is ConsentRequestOutcome.Failure -> ConsentFlowResult.Error(outcome.message)
            is ConsentRequestOutcome.Success -> handleSuccess(
                activity,
                outcome,
                requestPrivacyOptionsOnSatisfied,
            )
        }
    }

    suspend fun showPrivacyOptionsForm(activity: Activity) {
        consentRepository.showPrivacyOptionsForm(activity)
    }

    private suspend fun handleSuccess(
        activity: Activity,
        outcome: ConsentRequestOutcome.Success,
        requestPrivacyOptionsOnSatisfied: Boolean,
    ): ConsentFlowResult {
        return when (outcome.consentStatus) {
            ConsentInformation.ConsentStatus.REQUIRED -> {
                if (outcome.isConsentFormAvailable) {
                    val consentForm = runCatching { consentRepository.loadConsentForm(activity) }
                        .getOrElse { throwable ->
                            return ConsentFlowResult.Error(
                                throwable.message ?: throwable.localizedMessage,
                            )
                        }
                    ConsentFlowResult.ShowConsentForm(consentForm)
                } else {
                    ConsentFlowResult.Error("Consent form is required but not available.")
                }
            }

            ConsentInformation.ConsentStatus.OBTAINED,
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> {
                if (requestPrivacyOptionsOnSatisfied) {
                    ConsentFlowResult.ShowPrivacyOptionsForm
                } else {
                    ConsentFlowResult.ConsentSatisfied
                }
            }

            else -> {
                if (requestPrivacyOptionsOnSatisfied) {
                    ConsentFlowResult.ShowPrivacyOptionsForm
                } else {
                    ConsentFlowResult.ConsentSatisfied
                }
            }
        }
    }
}

sealed interface ConsentFlowResult {
    object ConsentSatisfied : ConsentFlowResult
    data class ShowConsentForm(val consentForm: ConsentForm) : ConsentFlowResult
    object ShowPrivacyOptionsForm : ConsentFlowResult
    data class Error(val message: String?) : ConsentFlowResult
}
