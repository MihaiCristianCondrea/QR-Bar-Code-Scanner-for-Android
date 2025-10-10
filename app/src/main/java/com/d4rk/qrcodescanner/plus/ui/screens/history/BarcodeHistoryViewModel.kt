package com.d4rk.qrcodescanner.plus.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarcodeHistoryViewModel(
    private val repository : BarcodeHistoryRepository
) : ViewModel() {

    private val _clearHistoryErrors = MutableSharedFlow<Throwable>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val clearHistoryErrors : SharedFlow<Throwable> = _clearHistoryErrors

    val historyCount : StateFlow<Int> = repository.observeHistoryCount()
        .distinctUntilChanged()
        .catch { throwable ->
            _clearHistoryErrors.emit(throwable)
            emit(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun clearHistory() {
        viewModelScope.launch {
            val result = runCatching { repository.clearHistory() }
            result.exceptionOrNull()?.let { throwable ->
                _clearHistoryErrors.emit(throwable)
            }
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
