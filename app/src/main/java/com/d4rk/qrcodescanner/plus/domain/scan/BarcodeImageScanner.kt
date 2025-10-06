package com.d4rk.qrcodescanner.plus.domain.scan
import android.graphics.Bitmap
import com.d4rk.qrcodescanner.plus.extension.orZero
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
object BarcodeImageScanner {
    private var bitmapBuffer: IntArray? = null
    suspend fun parse(image: Bitmap): Result {
        return withContext(Dispatchers.Default) {
            tryParse(image)
        }
    }
    private fun tryParse(image: Bitmap): Result {
        val width = image.width
        val height = image.height
        val size = width * height
        if (size > bitmapBuffer?.size.orZero()) {
            bitmapBuffer = IntArray(size)
        }
        bitmapBuffer?.let { image.getPixels(it, 0, width, 0, 0, width, height) }
        val source = RGBLuminanceSource(width, height, bitmapBuffer)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        return reader.decode(bitmap)
    }
}