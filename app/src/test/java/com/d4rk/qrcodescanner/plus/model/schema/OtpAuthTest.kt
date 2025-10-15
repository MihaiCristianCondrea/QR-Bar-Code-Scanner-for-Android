package com.d4rk.qrcodescanner.plus.model.schema

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OtpAuthTest {

    @Test
    fun toFormattedText_returnsLabelOrEmptyString() {
        val withLabel = OtpAuth(type = "totp", label = "Example")
        val withoutLabel = OtpAuth(type = "totp")

        assertThat(withLabel.toFormattedText()).isEqualTo("Example")
        assertThat(withoutLabel.toFormattedText()).isEmpty()
    }

    @Test
    fun toBarcodeText_includesEncodedQueryParameters() {
        val otpAuth = OtpAuth(
            type = "hotp",
            label = "Example:alice bob",
            issuer = "Example Inc",
            secret = "JBSWY3DPEHPK3PXP",
            algorithm = "SHA256",
            digits = 8,
            counter = 10,
            period = 45
        )

        val barcodeText = otpAuth.toBarcodeText()

        assertThat(barcodeText).isEqualTo(
            "otpauth://hotp/Example:alice%20bob?secret=JBSWY3DPEHPK3PXP&" +
                "issuer=Example%20Inc&algorithm=SHA256&digits=8&counter=10&period=45"
        )
    }
}
