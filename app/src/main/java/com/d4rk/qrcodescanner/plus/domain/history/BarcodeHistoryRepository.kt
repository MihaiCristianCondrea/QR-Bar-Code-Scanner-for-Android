package com.d4rk.qrcodescanner.plus.domain.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.d4rk.qrcodescanner.plus.model.Barcode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 20

enum class BarcodeHistoryFilter {
    ALL,
    FAVORITES
}

class BarcodeHistoryRepository(
    private val barcodeDatabase : BarcodeDatabase,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO
) {

    fun observeHistoryCount() : Flow<Int> {
        return barcodeDatabase.observeCount().flowOn(ioDispatcher)
    }

    fun observeHistory(filter : BarcodeHistoryFilter) : Flow<PagingData<Barcode>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE , enablePlaceholders = false)
        ) {
            when (filter) {
                BarcodeHistoryFilter.ALL -> barcodeDatabase.getAll()
                BarcodeHistoryFilter.FAVORITES -> barcodeDatabase.getFavorites()
            }
        }.flow
    }

    suspend fun clearHistory() {
        withContext(ioDispatcher) {
            barcodeDatabase.deleteAll()
        }
    }
}
