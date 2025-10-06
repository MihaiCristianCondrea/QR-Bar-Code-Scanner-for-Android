package com.d4rk.qrcodescanner.plus.ui.components.tiles

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.d4rk.qrcodescanner.plus.ui.screens.main.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class QuickSettingsTileService : TileService() {

    override fun onClick() {
        super.onClick()

        val intent = Intent(applicationContext , MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            this , 0 , intent , pendingIntentFlags
        )

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    // API 34+ (Android 14+): Proper and recommended way
                    startActivityAndCollapse(pendingIntent)
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // API 31–33: No support for PendingIntent version, fallback
                    unlockAndRun {
                        startActivity(intent)
                    }
                }

                else -> {
                    // Pre-Android 12: Safe to use legacy approach
                    startActivity(intent)
                }
            }
        } catch (e : SecurityException) {
            // Edge case: fallback when security restrictions block launching the activity
            unlockAndRun {
                startActivity(intent)
            }
        }
    }
}
