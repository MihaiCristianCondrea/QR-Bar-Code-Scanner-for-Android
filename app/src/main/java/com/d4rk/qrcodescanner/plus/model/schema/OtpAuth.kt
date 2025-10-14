package com.d4rk.qrcodescanner.plus.model.schema

import androidx.core.net.toUri
import java.io.Serializable

data class OtpAuth(
    val type: String? = null,
    val label: String? = null,
    val issuer: String? = null,
    val secret: String? = null,
    val algorithm: String? = null,
    val digits: Int? = null,
    val period: Long? = null,
    val counter: Long? = null
) : Schema, Serializable {
    companion object {
        const val TOTP_TYPE = "totp"
        const val HOTP_TYPE = "hotp"
        private const val URI_SCHEME = "otpauth"
        private const val SECRET_KEY = "secret"
        private const val ISSUER_KEY = "issuer"
        private const val ALGORITHM_KEY = "algorithm"
        private const val DIGITS_KEY = "digits"
        private const val LEGACY_DIGITS_KEY = "hint_digits"
        private const val COUNTER_KEY = "counter"
        private const val PERIOD_KEY = "period"
        fun parse(text: String): OtpAuth? {
            val uri = text.toUri()
            if (uri.scheme != URI_SCHEME) {
                return null
            }
            val type = uri.authority
            if (type != HOTP_TYPE && type != TOTP_TYPE) {
                return null
            }
            var label = uri.path?.trim()
            if (label?.startsWith('/') == true) {
                label = label.substring(1)
            }
            val issuer = uri.getQueryParameter(ISSUER_KEY)
            val secret = uri.getQueryParameter(SECRET_KEY)
            val algorithm = uri.getQueryParameter(ALGORITHM_KEY)
            val digits = uri.getQueryParameter(DIGITS_KEY)?.toIntOrNull()
                ?: uri.getQueryParameter(LEGACY_DIGITS_KEY)?.toIntOrNull()
            val period = uri.getQueryParameter(PERIOD_KEY)?.toLongOrNull()
            val counter = uri.getQueryParameter(COUNTER_KEY)?.toLongOrNull()
            return OtpAuth(type, label, issuer, secret, algorithm, digits, period, counter)
        }

        private const val UNRESERVED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
        private const val LABEL_ALLOWED_CHARS = "$UNRESERVED_CHARS:"

        fun encodeUriComponent(value: String, allow: String = UNRESERVED_CHARS): String {
            if (value.isEmpty()) {
                return value
            }

            val safeChars = allow.toSet()
            val builder = StringBuilder(value.length)
            value.forEach { char ->
                if (char in safeChars) {
                    builder.append(char)
                } else {
                    char.toString().toByteArray(Charsets.UTF_8).forEach { byte ->
                        builder.append('%')
                        builder.append(((byte.toInt()) and 0xFF).toString(16).uppercase().padStart(2, '0'))
                    }
                }
            }
            return builder.toString()
        }
    }

    override val schema = BarcodeSchema.OTP_AUTH
    override fun toFormattedText(): String {
        return label.orEmpty()
    }

    override fun toBarcodeText(): String {
        val safeType = requireNotNull(type)
        val path = encodeUriComponent(requireNotNull(label), LABEL_ALLOWED_CHARS)
        val queryParameters = listOfNotNull(
            secret?.takeIf { it.isNotBlank() }?.let { SECRET_KEY to it },
            issuer?.takeIf { it.isNotBlank() }?.let { ISSUER_KEY to it },
            algorithm?.takeIf { it.isNotBlank() }?.let { ALGORITHM_KEY to it },
            digits?.let { DIGITS_KEY to it.toString() },
            counter?.let { COUNTER_KEY to it.toString() },
            period?.let { PERIOD_KEY to it.toString() }
        )

        val query = queryParameters.joinToString(separator = "&") { (key, value) ->
            "${encodeUriComponent(key)}=${encodeUriComponent(value)}"
        }

        return buildString {
            append(URI_SCHEME)
            append("://")
            append(safeType)
            append('/')
            append(path)
            if (query.isNotEmpty()) {
                append('?')
                append(query)
            }
        }
    }
}