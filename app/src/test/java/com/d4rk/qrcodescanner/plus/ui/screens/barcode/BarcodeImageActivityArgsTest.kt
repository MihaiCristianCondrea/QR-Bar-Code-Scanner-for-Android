package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import android.content.Intent
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BarcodeImageActivityArgsTest {

    @Test
    fun `fromIntent returns args when barcode extra present`() {
        val barcode = sampleBarcode()
        val intent = Intent().apply {
            putExtras(BarcodeImageActivity.Args(barcode).toBundle())
        }

        val args = BarcodeImageActivity.Args.fromIntent(intent)

        assertEquals(barcode, args?.barcode)
    }

    @Test
    fun `fromIntent returns null when extra missing`() {
        val intent = Intent()

        val args = BarcodeImageActivity.Args.fromIntent(intent)

        assertNull(args)
    }

    private fun sampleBarcode(): Barcode {
        return Barcode(
            id = 1L,
            name = "Sample",
            text = "content",
            formattedText = "content",
            format = BarcodeFormat.QR_CODE,
            schema = BarcodeSchema.TEXT,
            date = 123456789L,
            isGenerated = true,
            isFavorite = false,
            errorCorrectionLevel = null,
            country = null
        )
    }
}
