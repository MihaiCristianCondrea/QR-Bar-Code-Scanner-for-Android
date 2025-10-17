package com.d4rk.qrcodescanner.plus.data.startup

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface ConsentRequestOutcome {
    data class Success(val consentStatus: Int, val isConsentFormAvailable: Boolean) :
        ConsentRequestOutcome

    data class Failure(val message: String?) : ConsentRequestOutcome
}

class ConsentManager(
    context: Context,
    private val consentDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    suspend fun requestConsent(activity: Activity): ConsentRequestOutcome =
        withContext(consentDispatcher) {
            suspendCancellableCoroutine { continuation ->
                val params = ConsentRequestParameters.Builder()
                    .setTagForUnderAgeOfConsent(false)
                    .build()
                consentInformation.requestConsentInfoUpdate(
                    activity,
                    params,
                    {
                        if (continuation.isActive) {
                            continuation.resume(
                                ConsentRequestOutcome.Success(
                                    consentStatus = consentInformation.consentStatus,
                                    isConsentFormAvailable = consentInformation.isConsentFormAvailable,
                                )
                            )
                        }
                    },
                    { formError ->
                        if (continuation.isActive) {
                            continuation.resume(
                                ConsentRequestOutcome.Failure(formError.message)
                            )
                        }
                    }
                )

                continuation.invokeOnCancellation {
                    // There is no cancellation API for the consent request, but we avoid
                    // resuming the continuation after cancellation by checking isActive.
                }
            }
        }

    suspend fun loadConsentForm(activity: Activity): ConsentForm =
        withContext(consentDispatcher) {
            suspendCancellableCoroutine { continuation ->
                UserMessagingPlatform.loadConsentForm(
                    activity,
                    { consentForm ->
                        if (continuation.isActive) {
                            continuation.resume(consentForm)
                        }
                    },
                    { formError ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception(formError.message))
                        }
                    }
                )

                continuation.invokeOnCancellation {
                    // No explicit cancellation API is provided by UMP.
                }
            }
        }
}
