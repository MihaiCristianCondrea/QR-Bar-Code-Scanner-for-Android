package com.d4rk.qrcodescanner.plus.model.schema

import com.d4rk.qrcodescanner.plus.utils.extension.joinToStringNotNullOrBlankWithLineSeparator
import com.d4rk.qrcodescanner.plus.utils.extension.removePrefixIgnoreCase
import com.d4rk.qrcodescanner.plus.utils.extension.startsWithIgnoreCase

data class Sms(
    val phone: String? = null, val message: String? = null
) : Schema {
    companion object {
        private const val PREFIX = "smsto:"
        private const val SEPARATOR = ":"
        fun parse(text: String): Sms? {
            if (text.startsWithIgnoreCase(PREFIX).not()) {
                return null
            }
            val parts = text.removePrefixIgnoreCase(PREFIX).split(SEPARATOR)
            return Sms(
                phone = parts.getOrNull(0), message = parts.getOrNull(1)
            )
        }
    }

    override val schema = BarcodeSchema.SMS
    override fun toFormattedText(): String {
        return listOf(phone, message).joinToStringNotNullOrBlankWithLineSeparator()
    }

    override fun toBarcodeText(): String {
        return PREFIX + phone.orEmpty() + "$SEPARATOR${message.orEmpty()}"
    }
}