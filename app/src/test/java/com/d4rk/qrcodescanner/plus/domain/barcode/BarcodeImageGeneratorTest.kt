package com.d4rk.qrcodescanner.plus.domain.barcode

import android.graphics.Bitmap
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.google.common.truth.Truth.assertThat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BarcodeImageGeneratorTest {

    @Test
    fun createHintsIncludesErrorCorrectionWhenProvided() {
        val hints = invokeCreateHints("H", margin = 2)

        assertThat(hints[EncodeHintType.ERROR_CORRECTION]).isEqualTo("H")
    }

    @Test
    fun createHintsOmitsErrorCorrectionWhenNotProvided() {
        val hints = invokeCreateHints(errorCorrectionLevel = null, margin = 1)

        assertThat(hints.containsKey(EncodeHintType.ERROR_CORRECTION)).isFalse()
    }

    @Test
    fun createSvgGeneratesRectanglesForSetBits() {
        val matrix = BitMatrix(2, 2)
        matrix.set(0, 0)
        matrix.set(1, 1)

        val svg = invokeCreateSvg(width = 4, height = 6, matrix = matrix)

        assertThat(svg)
            .startsWith("<svg width=\"4\" height=\"6\" viewBox=\"0 0 4 6\" xmlns=\"http://www.w3.org/2000/svg\">\n")
        assertThat(svg).contains("<rect x=\"0.0\" y=\"0.0\" width=\"2.0\" height=\"3.0\"/>\n")
        assertThat(svg).contains("<rect x=\"2.0\" y=\"3.0\" width=\"2.0\" height=\"3.0\"/>\n")
        assertThat(svg).endsWith("</svg>\n")
    }

    @Test
    fun createBitmapAppliesProvidedColorsToMatrixValues() {
        val matrix = BitMatrix(2, 2)
        matrix.set(0, 1)
        matrix.set(1, 0)
        val codeColor = 0xFF112233.toInt()
        val backgroundColor = 0xFFEEEEEE.toInt()

        val bitmap = invokeCreateBitmap(matrix, codeColor, backgroundColor)

        assertThat(bitmap.width).isEqualTo(2)
        assertThat(bitmap.height).isEqualTo(2)
        assertThat(bitmap.getPixel(0, 1)).isEqualTo(codeColor)
        assertThat(bitmap.getPixel(0, 0)).isEqualTo(backgroundColor)
    }

    @Test
    fun generateBitmapReturnsNullWhenWriterThrows() {
        val barcode = createSampleBarcode()

        val bitmap = BarcodeImageGenerator.generateBitmap(
            barcode = barcode,
            width = -10,
            height = 120
        )

        assertThat(bitmap).isNull()
    }

    private fun invokeCreateHints(
        errorCorrectionLevel: String?,
        margin: Int
    ): Map<EncodeHintType, Any> {
        val method = BarcodeImageGenerator::class.java.getDeclaredMethod(
            "createHints",
            String::class.java,
            Int::class.javaPrimitiveType
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(BarcodeImageGenerator, errorCorrectionLevel, margin) as Map<EncodeHintType, Any>
    }

    private fun invokeCreateSvg(
        width: Int,
        height: Int,
        matrix: BitMatrix
    ): String {
        val method = BarcodeImageGenerator::class.java.getDeclaredMethod(
            "createSvg",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            BitMatrix::class.java
        )
        method.isAccessible = true
        return method.invoke(BarcodeImageGenerator, width, height, matrix) as String
    }

    private fun invokeCreateBitmap(
        matrix: BitMatrix,
        codeColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val method = BarcodeImageGenerator::class.java.getDeclaredMethod(
            "createBitmap",
            BitMatrix::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        method.isAccessible = true
        return method.invoke(BarcodeImageGenerator, matrix, codeColor, backgroundColor) as Bitmap
    }

    private fun createSampleBarcode(): Barcode {
        return Barcode(
            text = "https://example.com",
            formattedText = "https://example.com",
            format = BarcodeFormat.QR_CODE,
            schema = BarcodeSchema.URL,
            date = 1_728_000_000_000L
        )
    }
}
