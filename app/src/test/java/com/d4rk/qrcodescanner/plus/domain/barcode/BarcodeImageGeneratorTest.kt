package com.d4rk.qrcodescanner.plus.domain.barcode

import com.google.common.truth.Truth.assertThat
import com.google.zxing.EncodeHintType
import org.junit.Test

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
}
