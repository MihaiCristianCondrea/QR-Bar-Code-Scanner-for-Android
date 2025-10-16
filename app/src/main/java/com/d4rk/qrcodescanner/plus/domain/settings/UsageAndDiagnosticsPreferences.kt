package com.d4rk.qrcodescanner.plus.domain.settings

import kotlinx.coroutines.flow.Flow

data class UsageAndDiagnosticsPreferences(
    val usageAndDiagnosticsEnabled: Boolean,
    val analyticsConsentGranted: Boolean,
    val adStorageConsentGranted: Boolean,
    val adUserDataConsentGranted: Boolean,
    val adPersonalizationConsentGranted: Boolean,
)

interface UsageAndDiagnosticsPreferencesRepository {
    fun observePreferences(): Flow<UsageAndDiagnosticsPreferences>

    suspend fun setUsageAndDiagnostics(enabled: Boolean)

    suspend fun setAnalyticsConsent(granted: Boolean)

    suspend fun setAdStorageConsent(granted: Boolean)

    suspend fun setAdUserDataConsent(granted: Boolean)

    suspend fun setAdPersonalizationConsent(granted: Boolean)
}
