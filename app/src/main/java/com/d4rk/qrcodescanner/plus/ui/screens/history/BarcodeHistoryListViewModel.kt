package com.d4rk.qrcodescanner.plus.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryFilter
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryRepository
import com.d4rk.qrcodescanner.plus.model.Barcode
import kotlinx.coroutines.flow.Flow

class BarcodeHistoryListViewModel(
    repository: BarcodeHistoryRepository,
    historyFilter: BarcodeHistoryFilter
) : ViewModel() {

    val history: Flow<PagingData<Barcode>> = repository.observeHistory(historyFilter)
        .cachedIn(viewModelScope)
}

class BarcodeHistoryListViewModelFactory(
    private val repository: BarcodeHistoryRepository,
    private val historyFilter: BarcodeHistoryFilter
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarcodeHistoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarcodeHistoryListViewModel(repository, historyFilter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
    }
}
