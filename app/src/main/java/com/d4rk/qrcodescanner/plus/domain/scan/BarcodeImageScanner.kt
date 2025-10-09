package com.d4rk.qrcodescanner.plus.domain.scan

import android.graphics.Bitmap
import com.d4rk.qrcodescanner.plus.utils.extension.toZxingFormat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object BarcodeImageScanner {
    private val scanner by lazy { BarcodeScanning.getClient() }

    suspend fun parse(image : Bitmap) : Barcode {
        return withContext(Dispatchers.Default) {
            val inputImage = InputImage.fromBitmap(image , 0)
            val barcodes = scanner.process(inputImage).await()
            barcodes.firstOrNull()?.takeIf { it.rawValue != null && it.format.toZxingFormat() != null }
                ?: throw NotFoundException.getNotFoundInstance()
        }
    }
}