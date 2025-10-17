package com.d4rk.qrcodescanner.plus.data.onboarding

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R

object OnboardingPreferences {
    private const val KEY_ONBOARDING_COMPLETE = "pref_onboarding_complete"
    private const val KEY_FRESH_INSTALL = "pref_fresh_install"

    fun ensureDefaultConsents(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val firebaseKey = context.getString(R.string.key_firebase)
        val usageKey = context.getString(R.string.key_usage_and_diagnostics)
        val personalizedAdsKey = context.getString(R.string.key_personalized_ads)
        val adStorageKey = context.getString(R.string.key_ad_storage_consent)
        val adUserDataKey = context.getString(R.string.key_ad_user_data_consent)

        preferences.edit {
            if (!preferences.contains(firebaseKey)) {
                putBoolean(firebaseKey, true)
            }
            if (!preferences.contains(usageKey)) {
                putBoolean(usageKey, true)
            }
            if (!preferences.contains(personalizedAdsKey)) {
                putBoolean(personalizedAdsKey, true)
            }
            if (!preferences.contains(adStorageKey)) {
                putBoolean(adStorageKey, true)
            }
            if (!preferences.contains(adUserDataKey)) {
                putBoolean(adUserDataKey, true)
            }
        }
    }

    fun isOnboardingComplete(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun setOnboardingComplete(context: Context, complete: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(KEY_ONBOARDING_COMPLETE, complete)
            }
    }

    fun isFreshInstall(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_FRESH_INSTALL, true)
    }

    fun setFreshInstall(context: Context, isFreshInstall: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(KEY_FRESH_INSTALL, isFreshInstall)
            }
    }

    fun isAnalyticsEnabled(context: Context): Boolean {
        val key = context.getString(R.string.key_firebase)
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(key, true)
    }

    fun setAnalyticsEnabled(context: Context, enabled: Boolean) {
        val key = context.getString(R.string.key_firebase)
        val usageKey = context.getString(R.string.key_usage_and_diagnostics)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(key, enabled)
                putBoolean(usageKey, enabled)
            }
    }

    fun isPersonalizedAdsEnabled(context: Context): Boolean {
        val key = context.getString(R.string.key_personalized_ads)
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(key, true)
    }

    fun setPersonalizedAdsEnabled(context: Context, enabled: Boolean) {
        val key = context.getString(R.string.key_personalized_ads)
        val adStorageKey = context.getString(R.string.key_ad_storage_consent)
        val adUserDataKey = context.getString(R.string.key_ad_user_data_consent)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(key, enabled)
                putBoolean(adStorageKey, enabled)
                putBoolean(adUserDataKey, enabled)
            }
    }
}
