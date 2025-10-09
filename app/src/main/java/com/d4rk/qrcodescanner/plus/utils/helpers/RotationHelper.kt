package com.d4rk.qrcodescanner.plus.utils.helpers

import android.content.Context
import android.content.pm.ActivityInfo
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

object RotationHelper {
    fun lockCurrentOrientationIfNeeded(activity : AppCompatActivity) {
        if (isAutoRotateOptionDisabled(activity)) {
            lockCurrentOrientation(activity)
        }
    }

    private fun isAutoRotateOptionDisabled(context : Context) : Boolean {
        val result = Settings.System.getInt(
            context.contentResolver , Settings.System.ACCELEROMETER_ROTATION , 0
        )
        return result == 0
    }

    private fun lockCurrentOrientation(activity : AppCompatActivity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
}