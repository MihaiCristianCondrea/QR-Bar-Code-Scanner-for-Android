package com.d4rk.qrcodescanner.plus.utils.helpers

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.tasks.await

/**
 * Helper utilities for triggering Google Play in-app updates.
 */
object InAppUpdateHelper {

    /**
     * Checks for an available update and starts the immediate update flow when possible.
     */
    suspend fun performUpdate(
        appUpdateManager: AppUpdateManager,
        updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        runCatching {
            val appUpdateInfo: AppUpdateInfo = appUpdateManager.appUpdateInfo.await()
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        launchImmediateUpdate(appUpdateManager, appUpdateInfo, updateResultLauncher)
                    }
                }

                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    launchImmediateUpdate(appUpdateManager, appUpdateInfo, updateResultLauncher)
                }

                else -> Unit
            }
        }
    }

    private fun launchImmediateUpdate(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
    }
}
