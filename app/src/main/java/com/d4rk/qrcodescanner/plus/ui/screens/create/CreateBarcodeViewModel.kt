package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CreateBarcodeViewModel(
    private val barcodeDatabase: BarcodeDatabase,
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    fun saveBarcode(barcode: Barcode): Flow<Barcode> {
        return flow {
            val savedBarcode = if (settings.saveCreatedBarcodesToHistory) {
                val id = barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
                barcode.copy(id = id)
            } else {
                barcode
            }
            emit(savedBarcode)
        }.flowOn(ioDispatcher)
    }

    fun readVCard(contentResolver: ContentResolver, uri: Uri): Flow<String> {
        return flow {
            val text = contentResolver.openInputStream(uri)?.use { stream ->
                stream.reader().use { reader -> reader.readText() }
            }
            emit(text.orEmpty())
        }.flowOn(ioDispatcher)
    }
}

class CreateBarcodeViewModelFactory(
    private val barcodeDatabase: BarcodeDatabase,
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateBarcodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateBarcodeViewModel(barcodeDatabase, settings, ioDispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
