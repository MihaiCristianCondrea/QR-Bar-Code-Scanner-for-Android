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
import kotlinx.coroutines.withContext

class CreateBarcodeViewModel(
    private val barcodeDatabase: BarcodeDatabase,
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    suspend fun saveBarcode(barcode: Barcode): Barcode {
        return withContext(ioDispatcher) {
            if (settings.saveCreatedBarcodesToHistory) {
                val id = barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
                barcode.copy(id = id)
            } else {
                barcode
            }
        }
    }

    suspend fun readVCard(contentResolver: ContentResolver, uri: Uri): String {
        return withContext(ioDispatcher) {
            contentResolver.openInputStream(uri)?.use { stream ->
                stream.reader().use { reader -> reader.readText() }
            }.orEmpty()
        }
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
