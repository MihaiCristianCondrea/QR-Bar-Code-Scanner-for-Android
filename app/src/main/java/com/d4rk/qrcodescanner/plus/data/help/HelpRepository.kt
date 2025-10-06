package com.d4rk.qrcodescanner.plus.data.help

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class HelpRepository(private val reviewManager : ReviewManager) {

    fun requestReviewFlow(onSuccess : (ReviewInfo) -> Unit , onFailure : (Exception) -> Unit) {
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                if (reviewInfo != null) {
                    onSuccess(reviewInfo)
                }
                else {
                    onFailure(IllegalStateException("ReviewInfo is null"))
                }
            }
            else {
                val exception = task.exception ?: Exception("Failed to request review flow")
                onFailure(exception)
            }
        }
    }

    fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo , onComplete : (Boolean) -> Unit) {
        reviewManager.launchReviewFlow(activity , reviewInfo).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}
