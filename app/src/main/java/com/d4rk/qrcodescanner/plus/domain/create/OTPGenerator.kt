package com.d4rk.qrcodescanner.plus.domain.create

import com.d4rk.qrcodescanner.plus.extension.decodeBase32
import com.d4rk.qrcodescanner.plus.extension.toHmacAlgorithm
import com.d4rk.qrcodescanner.plus.model.schema.OtpAuth
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.HmacOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.HmacOneTimePasswordGenerator
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import java.util.concurrent.TimeUnit

object OTPGenerator {
    private const val DEFAULT_DIGITS = 6
    private const val HOTP_INITIAL_COUNTER = 0L
    private const val TOTP_DEFAULT_PERIOD = 30L
    fun generateOTP(otp : OtpAuth) : String? {
        val secret = otp.secret.decodeBase32() ?: return null
        val algorithm = otp.algorithm.toHmacAlgorithm()
        val digits = otp.digits ?: DEFAULT_DIGITS
        val counter = otp.counter ?: HOTP_INITIAL_COUNTER
        val period = otp.period ?: TOTP_DEFAULT_PERIOD
        return when (otp.type) {
            OtpAuth.TOTP_TYPE -> generateTOTP(secret , period , digits , algorithm).toString()
            OtpAuth.HOTP_TYPE -> generateHOTP(secret , counter , digits , algorithm)
            else -> null
        }
    }

    private fun generateTOTP(secret : ByteArray , period : Long , digits : Int , algorithm : HmacAlgorithm) {
        val config = TimeBasedOneTimePasswordConfig(
            timeStep = period , timeStepUnit = TimeUnit.SECONDS , codeDigits = digits , hmacAlgorithm = algorithm
        )
        TimeBasedOneTimePasswordGenerator(secret , config)
    }

    private fun generateHOTP(secret : ByteArray , counter : Long , digits : Int , algorithm : HmacAlgorithm) : String {
        val config = HmacOneTimePasswordConfig(
            codeDigits = digits , hmacAlgorithm = algorithm
        )
        val generator = HmacOneTimePasswordGenerator(secret , config)
        return generator.generate(counter)
    }
}