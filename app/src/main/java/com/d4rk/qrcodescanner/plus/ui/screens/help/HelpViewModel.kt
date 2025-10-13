package com.d4rk.qrcodescanner.plus.ui.screens.help

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.d4rk.qrcodescanner.plus.data.help.HelpRepository
import com.d4rk.qrcodescanner.plus.data.help.ReviewLaunchResult
import com.d4rk.qrcodescanner.plus.data.help.ReviewRequestResult
import com.google.android.play.core.review.ReviewInfo

class HelpViewModel(private val repository: HelpRepository) : ViewModel() {

    suspend fun requestReview(): ReviewRequestResult {
        return runCatching { repository.requestReview() }
            .getOrElse { throwable -> ReviewRequestResult.Error(throwable) }
    }

    suspend fun launchReview(activity: Activity, reviewInfo: ReviewInfo): ReviewLaunchResult {
        return runCatching { repository.launchReview(activity, reviewInfo) }
            .getOrElse { throwable -> ReviewLaunchResult.Error(throwable) }
    }
}

class HelpViewModelFactory(private val repository: HelpRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HelpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
