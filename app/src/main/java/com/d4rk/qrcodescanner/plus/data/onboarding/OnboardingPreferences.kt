package com.d4rk.qrcodescanner.plus.data.onboarding

import android.content.Context
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R

object OnboardingPreferences {
    private const val KEY_ONBOARDING_COMPLETE = "pref_onboarding_complete"

    fun isOnboardingComplete(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun setOnboardingComplete(context: Context, complete: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_ONBOARDING_COMPLETE, complete)
            .apply()
    }

    fun isAnalyticsEnabled(context: Context): Boolean {
        val key = context.getString(R.string.key_firebase)
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(key, true)
    }

    fun setAnalyticsEnabled(context: Context, enabled: Boolean) {
        val key = context.getString(R.string.key_firebase)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, enabled)
            .apply()
    }

    fun isPersonalizedAdsEnabled(context: Context): Boolean {
        val key = context.getString(R.string.key_personalized_ads)
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(key, true)
    }

    fun setPersonalizedAdsEnabled(context: Context, enabled: Boolean) {
        val key = context.getString(R.string.key_personalized_ads)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, enabled)
            .apply()
    }
}
