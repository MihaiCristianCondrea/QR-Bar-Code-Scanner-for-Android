package com.d4rk.qrcodescanner.plus.ui.screens.settings.usage

import android.app.Activity
import android.util.Log
import com.d4rk.qrcodescanner.plus.data.startup.ConsentManager
import com.d4rk.qrcodescanner.plus.data.startup.ConsentRequestOutcome
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object UsageConsentHelper {

    private const val TAG = "UsageConsentHelper"

    suspend fun showConsentForm(activity: Activity) {
        val consentManager = ConsentManager(activity.applicationContext)
        val outcome = runCatching { consentManager.requestConsent(activity) }
            .getOrElse { throwable ->
                Log.e(TAG, "Failed to request consent info", throwable)
                return
            }

        when (outcome) {
            is ConsentRequestOutcome.Success -> handleConsentSuccess(activity, consentManager, outcome)
            is ConsentRequestOutcome.Failure -> Log.e(TAG, "Failed to request consent info: ${outcome.message}")
        }
    }

    private suspend fun handleConsentSuccess(
        activity: Activity,
        consentManager: ConsentManager,
        outcome: ConsentRequestOutcome.Success,
    ) {
        when (outcome.consentStatus) {
            ConsentInformation.ConsentStatus.REQUIRED -> {
                if (outcome.isConsentFormAvailable) {
                    val consentForm = runCatching { consentManager.loadConsentForm(activity) }
                        .getOrElse { throwable ->
                            Log.e(TAG, "Failed to load consent form", throwable)
                            return
                        }
                    showConsentForm(activity, consentForm)
                } else {
                    Log.w(TAG, "Consent form required but not available")
                }
            }

            ConsentInformation.ConsentStatus.OBTAINED,
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> {
                // No action required; consent already obtained or not needed.
            }

            else -> {
                Log.w(TAG, "Unhandled consent status: ${outcome.consentStatus}")
            }
        }
    }

    private suspend fun showConsentForm(
        activity: Activity,
        consentForm: ConsentForm,
    ) {
        suspendCancellableCoroutine { continuation ->
            try {
                consentForm.show(activity) {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "Failed to display consent form", throwable)
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            continuation.invokeOnCancellation {
                // No cancellation API is provided by the consent SDK.
            }
        }
    }
}
