package com.d4rk.qrcodescanner.plus.ui.screens.barcode.save

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
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

private const val IMAGE_SIZE = 640
private const val IMAGE_MARGIN = 2

class SaveBarcodeAsImageViewModel(
    private val barcodeImageGenerator: BarcodeImageGenerator,
    private val barcodeImageSaver: BarcodeImageSaver,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaveBarcodeAsImageUiState())
    val uiState: StateFlow<SaveBarcodeAsImageUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SaveBarcodeAsImageEvent>()
    val events: SharedFlow<SaveBarcodeAsImageEvent> = _events.asSharedFlow()

    fun saveBarcode(context: Context, barcode: Barcode, format: SaveBarcodeAsImageFormat) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isSaving = true) }
            val result = runCatching {
                val applicationContext = context.applicationContext
                when (format) {
                    SaveBarcodeAsImageFormat.PNG -> saveAsPng(applicationContext, barcode)
                    SaveBarcodeAsImageFormat.SVG -> saveAsSvg(applicationContext, barcode)
                }
            }
            _uiState.update { current -> current.copy(isSaving = false) }
            result.onSuccess {
                _events.emit(SaveBarcodeAsImageEvent.Success)
            }.onFailure { throwable ->
                _events.emit(SaveBarcodeAsImageEvent.Error(throwable))
            }
        }
    }

    private suspend fun saveAsPng(context: Context, barcode: Barcode) {
        val bitmap = withContext(defaultDispatcher) {
            barcodeImageGenerator.generateBitmap(barcode, IMAGE_SIZE, IMAGE_SIZE, IMAGE_MARGIN)
        } ?: throw IllegalStateException("Unable to generate barcode bitmap")
        withContext(ioDispatcher) {
            barcodeImageSaver.savePngImageToPublicDirectory(context, bitmap, barcode)
        }
    }

    private suspend fun saveAsSvg(context: Context, barcode: Barcode) {
        val svg = withContext(defaultDispatcher) {
            barcodeImageGenerator.generateSvg(barcode, IMAGE_SIZE, IMAGE_SIZE, IMAGE_MARGIN)
        }
        withContext(ioDispatcher) {
            barcodeImageSaver.saveSvgImageToPublicDirectory(context, svg, barcode)
        }
    }
}

data class SaveBarcodeAsImageUiState(
    val isSaving: Boolean = false
)

sealed interface SaveBarcodeAsImageEvent {
    data object Success : SaveBarcodeAsImageEvent
    data class Error(val throwable: Throwable) : SaveBarcodeAsImageEvent
}

enum class SaveBarcodeAsImageFormat {
    PNG,
    SVG;

    companion object {
        fun fromSpinnerIndex(index: Int): SaveBarcodeAsImageFormat? {
            return when (index) {
                0 -> PNG
                1 -> SVG
                else -> null
            }
        }
    }
}

class SaveBarcodeAsImageViewModelFactory(
    private val barcodeImageGenerator: BarcodeImageGenerator,
    private val barcodeImageSaver: BarcodeImageSaver,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SaveBarcodeAsImageViewModel::class.java)) {
            return SaveBarcodeAsImageViewModel(
                barcodeImageGenerator = barcodeImageGenerator,
                barcodeImageSaver = barcodeImageSaver,
                defaultDispatcher = defaultDispatcher,
                ioDispatcher = ioDispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
