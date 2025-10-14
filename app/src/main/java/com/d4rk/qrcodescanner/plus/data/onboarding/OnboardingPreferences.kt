package com.d4rk.qrcodescanner.plus.data.onboarding

import android.content.Context
import androidx.preference.PreferenceManager
import com.d4rk.qrcodescanner.plus.R
import androidx.core.content.edit

object OnboardingPreferences {
    private const val KEY_ONBOARDING_COMPLETE = "pref_onboarding_complete"
    private const val KEY_FRESH_INSTALL = "pref_fresh_install"

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
            .edit()
            .putBoolean(KEY_FRESH_INSTALL, isFreshInstall)
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
            .edit {
                putBoolean(key, enabled)
            }
    }

    fun isPersonalizedAdsEnabled(context: Context): Boolean {
        val key = context.getString(R.string.key_personalized_ads)
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(key, true)
    }

    fun setPersonalizedAdsEnabled(context: Context, enabled: Boolean) {
        val key = context.getString(R.string.key_personalized_ads)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(key, enabled)
            }
    }
}
