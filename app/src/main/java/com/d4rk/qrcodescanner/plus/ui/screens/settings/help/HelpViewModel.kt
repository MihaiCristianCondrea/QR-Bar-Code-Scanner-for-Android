package com.d4rk.qrcodescanner.plus.ui.screens.settings.help

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.d4rk.qrcodescanner.plus.data.help.HelpRepository
import com.google.android.play.core.review.ReviewInfo

class HelpViewModel(private val repository : HelpRepository) : ViewModel() {

    fun requestReviewFlow(onSuccess : (ReviewInfo) -> Unit , onFailure : (Exception) -> Unit) {
        repository.requestReviewFlow(onSuccess , onFailure)
    }

    fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo , onComplete : (Boolean) -> Unit) {
        repository.launchReviewFlow(activity , reviewInfo , onComplete)
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
