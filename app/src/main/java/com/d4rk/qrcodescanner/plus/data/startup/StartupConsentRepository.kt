package com.d4rk.qrcodescanner.plus.data.startup

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentForm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class StartupConsentRepository(
    private val consentManager: ConsentManager,
) {

    constructor(
        context: Context,
        consentDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ) : this(ConsentManager(context, consentDispatcher))

    suspend fun requestConsent(activity: Activity): ConsentRequestOutcome =
        consentManager.requestConsent(activity)

    suspend fun loadConsentForm(activity: Activity): ConsentForm =
        consentManager.loadConsentForm(activity)
}
