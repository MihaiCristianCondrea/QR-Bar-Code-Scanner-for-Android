package com.d4rk.qrcodescanner.plus.utils.helpers

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Utilities for launching the Google Play in-app review flow.
 */
object ReviewHelper {

    /**
     * Launches the in-app review dialog if the session count meets the threshold and the user
     * has not been prompted before. When the dialog is shown, [onReviewLaunched] is invoked.
     */
    fun launchInAppReviewIfEligible(
        activity: Activity,
        sessionCount: Int,
        hasPromptedBefore: Boolean,
        scope: CoroutineScope,
        onReviewLaunched: () -> Unit
    ) {
        if (sessionCount < 3 || hasPromptedBefore) return
        scope.launch {
            val launched = launchReview(activity)
            if (launched) {
                onReviewLaunched()
            }
        }
    }

    /**
     * Forces the in-app review dialog to display regardless of eligibility.
     */
    fun forceLaunchInAppReview(
        activity: Activity,
        scope: CoroutineScope
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            launchReview(activity)
        }
    }

    /**
     * Requests and launches the in-app review flow.
     *
     * @return `true` if the dialog was shown, `false` otherwise.
     */
    suspend fun launchReview(activity: Activity): Boolean {
        val reviewManager = ReviewManagerFactory.create(activity)
        return runCatching {
            val reviewInfo = reviewManager.requestReviewFlow().await()
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
            true
        }.getOrDefault(false)
    }
}
