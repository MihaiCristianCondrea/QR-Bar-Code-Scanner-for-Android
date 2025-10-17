package com.d4rk.qrcodescanner.plus.ui.screens.settings.usage

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ui.consent.ConsentFlowCoordinator
import com.d4rk.qrcodescanner.plus.ui.consent.ConsentFlowResult
import com.google.android.ump.ConsentForm
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object UsageConsentHelper {

    private const val TAG = "UsageConsentHelper"

    suspend fun showConsentForm(activity: Activity) {
        val consentCoordinator = ConsentFlowCoordinator(activity.applicationContext)
        val result = runCatching {
            consentCoordinator.prepareConsentUi(
                activity = activity,
                requestPrivacyOptionsOnSatisfied = true,
            )
        }.getOrElse { throwable ->
            Log.e(TAG, "Failed to prepare consent flow", throwable)
            showErrorToast(activity, throwable.message)
            return
        }

        when (result) {
            is ConsentFlowResult.ShowConsentForm -> presentConsentForm(activity, result.consentForm)
            ConsentFlowResult.ShowPrivacyOptionsForm -> showPrivacyOptions(activity, consentCoordinator)
            ConsentFlowResult.ConsentSatisfied -> Unit
            is ConsentFlowResult.Error -> {
                Log.e(TAG, "Consent flow error: ${result.message}")
                showErrorToast(activity, result.message)
            }
        }
    }

    private suspend fun presentConsentForm(activity: Activity, consentForm: ConsentForm) {
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

    private suspend fun showPrivacyOptions(
        activity: Activity,
        consentCoordinator: ConsentFlowCoordinator,
    ) {
        runCatching {
            consentCoordinator.showPrivacyOptionsForm(activity)
        }.onFailure { throwable ->
            Log.e(TAG, "Failed to display privacy options", throwable)
            showErrorToast(activity, throwable.message)
        }
    }

    private fun showErrorToast(activity: Activity, message: String?) {
        val text = message ?: activity.getString(R.string.consent_update_failed)
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
    }
}
