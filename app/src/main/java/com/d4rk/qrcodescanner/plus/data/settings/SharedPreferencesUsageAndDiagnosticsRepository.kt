package com.d4rk.qrcodescanner.plus.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferences
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SharedPreferencesUsageAndDiagnosticsRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UsageAndDiagnosticsPreferencesRepository {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources

    private val usageKey = resources.getString(R.string.key_usage_and_diagnostics)
    private val analyticsKey = resources.getString(R.string.key_firebase)
    private val adStorageKey = resources.getString(R.string.key_ad_storage_consent)
    private val adUserDataKey = resources.getString(R.string.key_ad_user_data_consent)
    private val adPersonalizationKey = resources.getString(R.string.key_personalized_ads)

    private val defaultUsageAndDiagnostics = !BuildConfig.DEBUG
    private val defaultAnalyticsConsent = true
    private val defaultAdStorageConsent = defaultUsageAndDiagnostics
    private val defaultAdUserDataConsent = defaultUsageAndDiagnostics
    private val defaultAdPersonalizationConsent = true

    private val trackedKeys = setOf(
        usageKey,
        analyticsKey,
        adStorageKey,
        adUserDataKey,
        adPersonalizationKey,
    )

    override fun observePreferences(): Flow<UsageAndDiagnosticsPreferences> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || trackedKeys.contains(key)) {
                trySend(readPreferences())
            }
        }

        trySend(readPreferences())

        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .flowOn(ioDispatcher)
        .conflate()
        .distinctUntilChanged()

    override suspend fun setUsageAndDiagnostics(enabled: Boolean) {
        withContext(ioDispatcher) {
            preferences.edit {
                putBoolean(usageKey, enabled)
                if (!enabled) {
                    putBoolean(analyticsKey, false)
                    putBoolean(adStorageKey, false)
                    putBoolean(adUserDataKey, false)
                    putBoolean(adPersonalizationKey, false)
                }
            }
        }
    }

    override suspend fun setAnalyticsConsent(granted: Boolean) {
        withContext(ioDispatcher) {
            preferences.edit { putBoolean(analyticsKey, granted) }
        }
    }

    override suspend fun setAdStorageConsent(granted: Boolean) {
        withContext(ioDispatcher) {
            preferences.edit { putBoolean(adStorageKey, granted) }
        }
    }

    override suspend fun setAdUserDataConsent(granted: Boolean) {
        withContext(ioDispatcher) {
            preferences.edit { putBoolean(adUserDataKey, granted) }
        }
    }

    override suspend fun setAdPersonalizationConsent(granted: Boolean) {
        withContext(ioDispatcher) {
            preferences.edit { putBoolean(adPersonalizationKey, granted) }
        }
    }

    private fun readPreferences(): UsageAndDiagnosticsPreferences {
        val usageEnabled = preferences.getBoolean(usageKey, defaultUsageAndDiagnostics)
        val analyticsConsent = preferences.getBoolean(analyticsKey, defaultAnalyticsConsent)
        val adStorageConsent = preferences.getBoolean(adStorageKey, defaultAdStorageConsent)
        val adUserDataConsent = preferences.getBoolean(adUserDataKey, defaultAdUserDataConsent)
        val adPersonalizationConsent =
            preferences.getBoolean(adPersonalizationKey, defaultAdPersonalizationConsent)

        return UsageAndDiagnosticsPreferences(
            usageAndDiagnosticsEnabled = usageEnabled,
            analyticsConsentGranted = analyticsConsent,
            adStorageConsentGranted = adStorageConsent,
            adUserDataConsentGranted = adUserDataConsent,
            adPersonalizationConsentGranted = adPersonalizationConsent,
        )
    }
}
