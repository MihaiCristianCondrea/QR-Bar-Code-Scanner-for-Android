package com.d4rk.qrcodescanner.plus.ui.screens.settings.usage

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object UsageConsentHelper {

    private const val TAG = "UsageConsentHelper"

    suspend fun showConsentForm(activity: Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        suspendCancellableCoroutine { continuation ->
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    loadAndShowForm(activity, continuation::resume)
                },
                { formError ->
                    Log.e(TAG, "Failed to request consent info: ${formError.message}")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                },
            )

            continuation.invokeOnCancellation {
                // No specific cancellation API available from the consent SDK.
            }
        }
    }

    private fun loadAndShowForm(
        activity: Activity,
        onFinished: (Unit) -> Unit,
    ) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm: ConsentForm ->
                runCatching {
                    consentForm.show(activity) {
                        onFinished(Unit)
                    }
                }.onFailure { throwable ->
                    Log.e(TAG, "Failed to display consent form", throwable)
                    onFinished(Unit)
                }
            },
            { formError ->
                Log.e(TAG, "Failed to load consent form: ${formError.message}")
                onFinished(Unit)
            },
        )
    }
}
