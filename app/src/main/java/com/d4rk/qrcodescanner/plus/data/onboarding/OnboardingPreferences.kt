package com.d4rk.qrcodescanner.plus.data.onboarding

import android.content.Context
import androidx.preference.PreferenceManager

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
}
