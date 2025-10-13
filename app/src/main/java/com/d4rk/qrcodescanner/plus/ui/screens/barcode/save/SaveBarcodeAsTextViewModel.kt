package com.d4rk.qrcodescanner.plus.ui.screens.barcode.save

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.model.Barcode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SaveBarcodeAsTextUiState(
    val isSaving: Boolean = false
)

sealed interface SaveBarcodeAsTextEvent {
    data object Success : SaveBarcodeAsTextEvent
    data class Error(val throwable: Throwable) : SaveBarcodeAsTextEvent
}

enum class SaveBarcodeAsTextFormat {
    CSV,
    JSON;

    companion object {
        fun fromSpinnerIndex(index: Int): SaveBarcodeAsTextFormat? {
            return when (index) {
                0 -> CSV
                1 -> JSON
                else -> null
            }
        }
    }
}

class SaveBarcodeAsTextViewModel(
    private val barcodeSaver: BarcodeSaver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaveBarcodeAsTextUiState())
    val uiState: StateFlow<SaveBarcodeAsTextUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SaveBarcodeAsTextEvent>()
    val events: SharedFlow<SaveBarcodeAsTextEvent> = _events.asSharedFlow()

    fun saveBarcode(context: Context, barcode: Barcode, format: SaveBarcodeAsTextFormat) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isSaving = true) }
            val result = runCatching {
                val applicationContext = context.applicationContext
                when (format) {
                    SaveBarcodeAsTextFormat.CSV -> saveCsv(applicationContext, barcode)
                    SaveBarcodeAsTextFormat.JSON -> saveJson(applicationContext, barcode)
                }
            }
            _uiState.update { current -> current.copy(isSaving = false) }
            result.onSuccess {
                _events.emit(SaveBarcodeAsTextEvent.Success)
            }.onFailure { throwable ->
                _events.emit(SaveBarcodeAsTextEvent.Error(throwable))
            }
        }
    }

    private suspend fun saveCsv(context: Context, barcode: Barcode) {
        withContext(ioDispatcher) {
            barcodeSaver.saveBarcodeAsCsv(context, barcode)
        }
    }

    private suspend fun saveJson(context: Context, barcode: Barcode) {
        withContext(ioDispatcher) {
            barcodeSaver.saveBarcodeAsJson(context, barcode)
        }
    }
}

class SaveBarcodeAsTextViewModelFactory(
    private val barcodeSaver: BarcodeSaver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SaveBarcodeAsTextViewModel::class.java)) {
            return SaveBarcodeAsTextViewModel(
                barcodeSaver = barcodeSaver,
                ioDispatcher = ioDispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
