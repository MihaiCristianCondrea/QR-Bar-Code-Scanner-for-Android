package com.d4rk.qrcodescanner.plus.data.startup

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

sealed interface ConsentRequestOutcome {
    data class Success(
        val consentStatus: Int,
        val isConsentFormAvailable: Boolean,
    ) : ConsentRequestOutcome

    data class Failure(val message: String?) : ConsentRequestOutcome
}

class StartupConsentRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun requestConsent(activity: Activity): Flow<ConsentRequestOutcome> = callbackFlow {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                trySend(
                    ConsentRequestOutcome.Success(
                        consentStatus = consentInformation.consentStatus,
                        isConsentFormAvailable = consentInformation.isConsentFormAvailable,
                    )
                )
                close()
            },
            { formError ->
                trySend(ConsentRequestOutcome.Failure(formError.message))
                close()
            }
        )
        awaitClose { }
    }.flowOn(ioDispatcher)

    fun loadConsentForm(activity: Activity): Flow<ConsentForm> = callbackFlow {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm ->
                trySend(consentForm)
                close()
            },
            { formError ->
                close(Exception(formError.message))
            }
        )
        awaitClose { }
    }.flowOn(ioDispatcher)
}
