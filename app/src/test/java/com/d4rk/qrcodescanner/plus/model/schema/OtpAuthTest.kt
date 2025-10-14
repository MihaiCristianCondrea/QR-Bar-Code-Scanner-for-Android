package com.d4rk.qrcodescanner.plus.model.schema

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OtpAuthTest {

    @Test
    fun `toBarcodeText emits canonical digits parameter`() {
        val otp = OtpAuth(
            type = OtpAuth.TOTP_TYPE,
            label = "Example:user",
            secret = "SECRET",
            digits = 8,
            period = 30
        )

        val barcodeText = otp.toBarcodeText()
        val uri = Uri.parse(barcodeText)

        assertThat(uri.getQueryParameter("digits")).isEqualTo("8")
        assertThat(uri.getQueryParameter("hint_digits")).isNull()

        val reparsed = OtpAuth.parse(barcodeText)
        assertThat(reparsed?.digits).isEqualTo(8)
    }

    @Test
    fun `parse falls back to legacy hint_digits parameter`() {
        val legacyBarcode = "otpauth://totp/Example:user?secret=SECRET&hint_digits=7"

        val parsed = OtpAuth.parse(legacyBarcode)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.digits).isEqualTo(7)

        val canonicalized = parsed!!.toBarcodeText()
        val uri = Uri.parse(canonicalized)
        assertThat(uri.getQueryParameter("digits")).isEqualTo("7")
        assertThat(uri.getQueryParameter("hint_digits")).isNull()
    }
}
