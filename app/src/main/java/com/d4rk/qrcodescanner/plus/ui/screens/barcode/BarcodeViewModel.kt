package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeDetailsRepository
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.ParsedBarcode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class BarcodeViewModel(
    initialBarcode: Barcode,
    private val repository: BarcodeDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeUiState(initialBarcode))
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BarcodeEvent>()
    val events: SharedFlow<BarcodeEvent> = _events.asSharedFlow()

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (!currentState.isInDatabase) {
            return
        }
        val updatedBarcode = currentState.barcode.copy(isFavorite = currentState.barcode.isFavorite.not())
        repository.saveBarcode(updatedBarcode, avoidDuplicates = false)
            .onStart { setProcessing(true) }
            .map { rowId -> updatedBarcode.copy(id = updatedBarcode.id.takeIf { it != 0L } ?: rowId) }
            .onEach { savedBarcode ->
                setProcessing(false, savedBarcode)
                _events.emit(BarcodeEvent.FavoriteToggled(savedBarcode.isFavorite))
            }
            .catch { throwable ->
                setProcessing(false)
                emitError(throwable)
            }
            .launchIn(viewModelScope)
    }

    fun updateName(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return
        }
        val currentState = _uiState.value
        if (!currentState.isInDatabase) {
            return
        }
        val updatedBarcode = currentState.barcode.copy(name = trimmedName)
        repository.saveBarcode(updatedBarcode, avoidDuplicates = false)
            .onStart { setProcessing(true) }
            .map { rowId -> updatedBarcode.copy(id = updatedBarcode.id.takeIf { it != 0L } ?: rowId) }
            .onEach { savedBarcode ->
                setProcessing(false, savedBarcode)
                _events.emit(BarcodeEvent.NameUpdated(trimmedName))
            }
            .catch { throwable ->
                setProcessing(false)
                emitError(throwable)
            }
            .launchIn(viewModelScope)
    }

    fun saveBarcode(avoidDuplicates: Boolean) {
        val currentState = _uiState.value
        if (currentState.isInDatabase) {
            return
        }
        val barcodeToSave = currentState.barcode
        repository.saveBarcode(barcodeToSave, avoidDuplicates)
            .onStart { setProcessing(true) }
            .map { rowId -> barcodeToSave.copy(id = rowId) }
            .onEach { savedBarcode ->
                setProcessing(false, savedBarcode)
                _events.emit(BarcodeEvent.BarcodeSaved(savedBarcode))
            }
            .catch { throwable ->
                setProcessing(false)
                emitError(throwable)
            }
            .launchIn(viewModelScope)
    }

    fun deleteBarcode() {
        val currentState = _uiState.value
        val barcodeId = currentState.barcode.id
        if (barcodeId == 0L) {
            return
        }
        repository.deleteBarcode(barcodeId)
            .onStart { setDeleting(true) }
            .onEach {
                setDeleting(false)
                _events.emit(BarcodeEvent.BarcodeDeleted)
            }
            .catch { throwable ->
                setDeleting(false)
                emitError(throwable)
            }
            .launchIn(viewModelScope)
    }

    private fun setProcessing(isProcessing: Boolean, barcode: Barcode? = null) {
        _uiState.update { current ->
            val targetBarcode = barcode ?: current.barcode
            current.copy(
                barcode = targetBarcode,
                parsedBarcode = if (barcode != null) ParsedBarcode(targetBarcode) else current.parsedBarcode,
                isProcessing = isProcessing
            )
        }
    }

    private fun setDeleting(isDeleting: Boolean) {
        _uiState.update { current ->
            current.copy(isDeleting = isDeleting)
        }
    }

    private suspend fun emitError(throwable: Throwable) {
        _events.emit(BarcodeEvent.Error(throwable))
    }
}

sealed interface BarcodeEvent {
    data class FavoriteToggled(val isFavorite: Boolean) : BarcodeEvent
    data class NameUpdated(val name: String) : BarcodeEvent
    data class BarcodeSaved(val barcode: Barcode) : BarcodeEvent
    data object BarcodeDeleted : BarcodeEvent
    data class Error(val throwable: Throwable) : BarcodeEvent
}

data class BarcodeUiState(
    val barcode: Barcode,
    val parsedBarcode: ParsedBarcode = ParsedBarcode(barcode),
    val isProcessing: Boolean = false,
    val isDeleting: Boolean = false
) {
    val isInDatabase: Boolean = barcode.id != 0L
}

class BarcodeViewModelFactory(
    private val initialBarcode: Barcode,
    private val repository: BarcodeDetailsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarcodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarcodeViewModel(initialBarcode, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
