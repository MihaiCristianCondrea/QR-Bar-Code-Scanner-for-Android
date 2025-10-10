package com.d4rk.qrcodescanner.plus.ui.screens.scan.file

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.google.zxing.NotFoundException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

data class ScanBarcodeFromFileUiState(
    val selectedImageUri : Uri? = null ,
    val isScanning : Boolean = false ,
) {
    val isScanButtonEnabled : Boolean get() = selectedImageUri != null && !isScanning
}

sealed interface ScanBarcodeFromFileEvent {
    data class NavigateToBarcode(val barcode : Barcode) : ScanBarcodeFromFileEvent
    data class ShowError(val throwable : Throwable) : ScanBarcodeFromFileEvent
}

class ScanBarcodeFromFileViewModel(
    application : Application ,
    private val barcodeImageScanner : BarcodeImageScanner ,
    private val barcodeParser : BarcodeParser ,
    private val barcodeDatabase : BarcodeDatabase ,
    private val settings : Settings ,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO ,
) : AndroidViewModel(application) {

    private val contentResolver get() = getApplication<Application>().contentResolver

    private val _uiState = MutableStateFlow(ScanBarcodeFromFileUiState())
    val uiState : StateFlow<ScanBarcodeFromFileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScanBarcodeFromFileEvent>()
    val events : SharedFlow<ScanBarcodeFromFileEvent> = _events.asSharedFlow()

    private var scanJob : Job? = null

    fun onImagePicked(uri : Uri) {
        scanJob?.cancel()
        scanJob = null
        _uiState.update { current ->
            current.copy(selectedImageUri = uri , isScanning = false)
        }
    }

    fun scanSelectedImage() {
        val uri = _uiState.value.selectedImageUri ?: return
        scanJob?.cancel()
        scanJob = scanBarcodeFromUri(uri)
            .onStart {
                _uiState.update { current -> current.copy(isScanning = true) }
            }
            .onEach { barcode -> handleScanSuccess(barcode) }
            .catch { throwable -> handleScanFailure(throwable) }
            .launchIn(viewModelScope)
    }

    private fun scanBarcodeFromUri(uri : Uri) = flow {
        val bitmap = decodeBitmap(uri)
        val mlKitBarcode = barcodeImageScanner.parse(bitmap)
        val barcode = barcodeParser.parse(mlKitBarcode)
            ?: throw NotFoundException.getNotFoundInstance()
        emit(barcode)
    }.flowOn(ioDispatcher)

    private suspend fun handleScanSuccess(barcode : Barcode) {
        if (settings.saveScannedBarcodesToHistory.not()) {
            completeScan()
            _events.emit(ScanBarcodeFromFileEvent.NavigateToBarcode(barcode))
            return
        }

        persistBarcode(barcode)
            .onEach { id ->
                completeScan()
                _events.emit(ScanBarcodeFromFileEvent.NavigateToBarcode(barcode.copy(id = id)))
            }
            .catch { throwable ->
                completeScan()
                _events.emit(ScanBarcodeFromFileEvent.ShowError(throwable))
            }
            .launchIn(viewModelScope)
    }

    private suspend fun handleScanFailure(throwable : Throwable) {
        completeScan()
        if (throwable !is NotFoundException) {
            _events.emit(ScanBarcodeFromFileEvent.ShowError(throwable))
        }
    }

    private fun persistBarcode(barcode : Barcode) = flow {
        val id = barcodeDatabase.save(barcode , settings.doNotSaveDuplicates)
        emit(id)
    }.flowOn(ioDispatcher)

    private fun completeScan() {
        _uiState.update { current ->
            current.copy(isScanning = false)
        }
        scanJob = null
    }

    private fun decodeBitmap(uri : Uri) : Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver , uri)
            ImageDecoder.decodeBitmap(source)
        }
        else {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } ?: throw IllegalStateException("Unable to decode image: $uri")
    }
}

class ScanBarcodeFromFileViewModelFactory(
    private val application : Application ,
    private val barcodeImageScanner : BarcodeImageScanner ,
    private val barcodeParser : BarcodeParser ,
    private val barcodeDatabase : BarcodeDatabase ,
    private val settings : Settings ,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO ,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass : Class<T>) : T {
        if (modelClass.isAssignableFrom(ScanBarcodeFromFileViewModel::class.java)) {
            return ScanBarcodeFromFileViewModel(
                application = application ,
                barcodeImageScanner = barcodeImageScanner ,
                barcodeParser = barcodeParser ,
                barcodeDatabase = barcodeDatabase ,
                settings = settings ,
                ioDispatcher = ioDispatcher
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
