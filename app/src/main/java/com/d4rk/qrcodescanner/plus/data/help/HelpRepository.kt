package com.d4rk.qrcodescanner.plus.data.help

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn

class HelpRepository(
    private val reviewManager : ReviewManager ,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO ,
    private val mainDispatcher : CoroutineDispatcher = Dispatchers.Main
) {

    fun requestReviewFlow() : Flow<ReviewRequestResult> = callbackFlow {
        val requestTask = reviewManager.requestReviewFlow()
        requestTask.addOnCompleteListener { task ->
            val result = if (task.isSuccessful) {
                val reviewInfo = task.result
                if (reviewInfo != null) {
                    ReviewRequestResult.Success(reviewInfo)
                }
                else {
                    ReviewRequestResult.Error(IllegalStateException("ReviewInfo is null"))
                }
            }
            else {
                val exception = task.exception ?: Exception("Failed to request review flow")
                ReviewRequestResult.Error(exception)
            }
            trySend(result).isSuccess
            close()
        }

        awaitClose { }
    }
        .flowOn(ioDispatcher)
        .conflate()

    fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) : Flow<ReviewLaunchResult> = callbackFlow {
        val launchTask = reviewManager.launchReviewFlow(activity , reviewInfo)
        launchTask.addOnCompleteListener { task ->
            val result = if (task.isSuccessful) {
                ReviewLaunchResult.Success
            }
            else {
                val exception = task.exception ?: Exception("Failed to launch review flow")
                ReviewLaunchResult.Error(exception)
            }
            trySend(result).isSuccess
            close()
        }

        awaitClose { }
    }
        .flowOn(mainDispatcher)
        .conflate()
}

sealed interface ReviewRequestResult {
    data class Success(val reviewInfo : ReviewInfo) : ReviewRequestResult
    data class Error(val throwable : Throwable) : ReviewRequestResult
}

sealed interface ReviewLaunchResult {
    data object Success : ReviewLaunchResult
    data class Error(val throwable : Throwable) : ReviewLaunchResult
}
