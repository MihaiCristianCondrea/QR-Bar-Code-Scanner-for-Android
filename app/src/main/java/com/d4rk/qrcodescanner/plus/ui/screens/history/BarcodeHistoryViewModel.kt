package com.d4rk.qrcodescanner.plus.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarcodeHistoryViewModel(
    private val repository : BarcodeHistoryRepository
) : ViewModel() {

    val historyCount : StateFlow<Int> = repository.observeHistoryCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private val _clearHistoryErrors = MutableSharedFlow<Throwable>()
    val clearHistoryErrors : SharedFlow<Throwable> = _clearHistoryErrors

    fun clearHistory() {
        viewModelScope.launch {
            runCatching { repository.clearHistory() }
                .onFailure { throwable -> _clearHistoryErrors.emit(throwable) }
        }
    }
}

class BarcodeHistoryViewModelFactory(
    private val repository : BarcodeHistoryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass : Class<T>) : T {
        if (modelClass.isAssignableFrom(BarcodeHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarcodeHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
    }
}
