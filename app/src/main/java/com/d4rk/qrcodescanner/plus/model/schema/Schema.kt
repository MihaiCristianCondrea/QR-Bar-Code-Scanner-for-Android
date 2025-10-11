package com.d4rk.qrcodescanner.plus.model.schema

enum class BarcodeSchema {
    APP, BOOKMARK, CRYPTOCURRENCY, EMAIL, GEO, GOOGLE_MAPS, MMS, MECARD, OTP_AUTH, PHONE, SMS, URL, VEVENT, VCARD, WIFI, YOUTUBE, NZCOVIDTRACER, BOARDINGPASS, OTHER;
}

interface Schema {
    val schema: BarcodeSchema
    fun toFormattedText(): String
    fun toBarcodeText(): String
}