package com.d4rk.qrcodescanner.plus.model.schema

class Other(val text : String) : Schema {
    override val schema = BarcodeSchema.OTHER
    override fun toFormattedText() : String = text
    override fun toBarcodeText() : String = text
}