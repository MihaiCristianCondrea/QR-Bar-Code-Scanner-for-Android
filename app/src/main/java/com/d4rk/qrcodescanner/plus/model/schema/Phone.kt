package com.d4rk.qrcodescanner.plus.model.schema

import com.d4rk.qrcodescanner.plus.extension.removePrefixIgnoreCase
import com.d4rk.qrcodescanner.plus.extension.startsWithIgnoreCase

class Phone(val phone : String) : Schema {

    companion object {
        private const val PREFIX = "tel:"

        fun parse(text : String) : Phone? {
            if (text.startsWithIgnoreCase(PREFIX).not()) {
                return null
            }

            val phone = text.removePrefixIgnoreCase(PREFIX)
            return Phone(phone)
        }
    }

    override val schema = BarcodeSchema.PHONE
    override fun toFormattedText() : String = phone
    override fun toBarcodeText() : String = "$PREFIX$phone"
}