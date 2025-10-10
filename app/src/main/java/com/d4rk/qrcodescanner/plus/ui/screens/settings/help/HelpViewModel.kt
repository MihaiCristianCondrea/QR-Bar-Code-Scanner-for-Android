package com.d4rk.qrcodescanner.plus.ui.screens.settings.help

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.d4rk.qrcodescanner.plus.data.help.HelpRepository
import com.d4rk.qrcodescanner.plus.data.help.ReviewLaunchResult
import com.d4rk.qrcodescanner.plus.data.help.ReviewRequestResult
import com.google.android.play.core.review.ReviewInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class HelpViewModel(private val repository : HelpRepository) : ViewModel() {

    fun requestReviewFlow() : Flow<ReviewRequestResult> {
        return repository.requestReviewFlow().catch { throwable ->
            if (throwable is CancellationException) {
                throw throwable
            }
            emit(ReviewRequestResult.Error(throwable))
        }
    }

    fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) : Flow<ReviewLaunchResult> {
        return repository.launchReviewFlow(activity , reviewInfo).catch { throwable ->
            if (throwable is CancellationException) {
                throw throwable
            }
            emit(ReviewLaunchResult.Error(throwable))
        }
    }
}

class HelpViewModelFactory(private val repository : HelpRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass : Class<T>) : T {
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HelpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
