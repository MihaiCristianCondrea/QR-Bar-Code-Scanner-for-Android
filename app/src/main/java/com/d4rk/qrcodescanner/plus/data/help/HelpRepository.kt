package com.d4rk.qrcodescanner.plus.data.help

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class HelpRepository(
    private val reviewManager: ReviewManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    suspend fun requestReview(): ReviewRequestResult = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            val requestTask = reviewManager.requestReviewFlow()
            requestTask.addOnCompleteListener { task ->
                if (!continuation.isActive) {
                    return@addOnCompleteListener
                }
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    if (reviewInfo != null) {
                        continuation.resume(ReviewRequestResult.Success(reviewInfo))
                    } else {
                        continuation.resume(ReviewRequestResult.Error(IllegalStateException("ReviewInfo is null")))
                    }
                } else {
                    val exception = task.exception ?: Exception("Failed to request review flow")
                    continuation.resume(ReviewRequestResult.Error(exception))
                }
            }
        }
    }

    suspend fun launchReview(activity: Activity, reviewInfo: ReviewInfo): ReviewLaunchResult =
        withContext(mainDispatcher) {
            suspendCancellableCoroutine { continuation ->
                val launchTask = reviewManager.launchReviewFlow(activity, reviewInfo)
                launchTask.addOnCompleteListener { task ->
                    if (!continuation.isActive) {
                        return@addOnCompleteListener
                    }
                    if (task.isSuccessful) {
                        continuation.resume(ReviewLaunchResult.Success)
                    } else {
                        val exception = task.exception ?: Exception("Failed to launch review flow")
                        continuation.resume(ReviewLaunchResult.Error(exception))
                    }
                }
            }
        }
}

sealed interface ReviewRequestResult {
    data class Success(val reviewInfo: ReviewInfo) : ReviewRequestResult
    data class Error(val throwable: Throwable) : ReviewRequestResult
}

sealed interface ReviewLaunchResult {
    data object Success : ReviewLaunchResult
    data class Error(val throwable: Throwable) : ReviewLaunchResult
}
