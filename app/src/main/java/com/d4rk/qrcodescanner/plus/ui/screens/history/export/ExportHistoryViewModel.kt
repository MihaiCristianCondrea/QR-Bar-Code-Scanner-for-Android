package com.d4rk.qrcodescanner.plus.ui.screens.history.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.model.ExportBarcode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportHistoryViewModel(
    private val barcodeDatabase: BarcodeDatabase,
    private val barcodeSaver: BarcodeSaver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val exportRequests = MutableSharedFlow<ExportHistoryRequest>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExportHistoryUiState> = exportRequests
        .flatMapLatest { request ->
            flow {
                emit(ExportHistoryUiState.Loading)
                try {
                    val barcodes = withContext(ioDispatcher) {
                        barcodeDatabase.getAllForExport().first()
                    }
                    request.exportType.save(
                        barcodeSaver = barcodeSaver,
                        context = request.context,
                        fileName = request.fileName,
                        barcodes = barcodes
                    )
                    emit(ExportHistoryUiState.Success)
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    emit(ExportHistoryUiState.Error(throwable))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExportHistoryUiState.Idle
        )

    fun exportHistory(context: Context, fileName: String, exportType: ExportType) {
        viewModelScope.launch {
            exportRequests.emit(
                ExportHistoryRequest(
                    context = context.applicationContext,
                    fileName = fileName,
                    exportType = exportType
                )
            )
        }
    }
}

private data class ExportHistoryRequest(
    val context: Context,
    val fileName: String,
    val exportType: ExportType
)

sealed interface ExportHistoryUiState {
    data object Idle : ExportHistoryUiState
    data object Loading : ExportHistoryUiState
    data object Success : ExportHistoryUiState
    data class Error(val throwable: Throwable) : ExportHistoryUiState
}

enum class ExportType {
    CSV,
    JSON;

    suspend fun save(
        barcodeSaver: BarcodeSaver,
        context: Context,
        fileName: String,
        barcodes: List<ExportBarcode>
    ) {
        when (this) {
            CSV -> barcodeSaver.saveBarcodeHistoryAsCsv(context, fileName, barcodes)
            JSON -> barcodeSaver.saveBarcodeHistoryAsJson(context, fileName, barcodes)
        }
    }

    companion object {
        fun fromSpinnerIndex(index: Int): ExportType? {
            return when (index) {
                0 -> CSV
                1 -> JSON
                else -> null
            }
        }
    }
}

class ExportHistoryViewModelFactory(
    private val barcodeDatabase: BarcodeDatabase,
    private val barcodeSaver: BarcodeSaver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportHistoryViewModel(barcodeDatabase, barcodeSaver, ioDispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
