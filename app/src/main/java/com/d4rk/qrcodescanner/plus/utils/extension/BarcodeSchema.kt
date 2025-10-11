package com.d4rk.qrcodescanner.plus.utils.extension

import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema

fun BarcodeSchema.toImageId(): Int? {
    return when (this) {
        BarcodeSchema.BOOKMARK -> R.drawable.ic_bookmark
        BarcodeSchema.CRYPTOCURRENCY -> R.drawable.ic_bitcoin
        BarcodeSchema.EMAIL -> R.drawable.ic_email
        BarcodeSchema.GEO -> R.drawable.ic_location
        BarcodeSchema.APP -> R.drawable.ic_app
        BarcodeSchema.MMS -> R.drawable.ic_mms
        BarcodeSchema.MECARD -> R.drawable.ic_contact
        BarcodeSchema.PHONE -> R.drawable.ic_phone
        BarcodeSchema.OTP_AUTH -> R.drawable.ic_otp
        BarcodeSchema.SMS -> R.drawable.ic_sms
        BarcodeSchema.URL -> R.drawable.ic_link
        BarcodeSchema.VEVENT -> R.drawable.ic_calendar
        BarcodeSchema.VCARD -> R.drawable.ic_contact
        BarcodeSchema.WIFI -> R.drawable.ic_wifi
        BarcodeSchema.YOUTUBE -> R.drawable.ic_youtube
        BarcodeSchema.BOARDINGPASS -> R.drawable.ic_boardingpass
        else -> null
    }
}

fun BarcodeSchema.toStringId(): Int? {
    return when (this) {
        BarcodeSchema.BOOKMARK -> R.string.bookmark
        BarcodeSchema.CRYPTOCURRENCY -> R.string.bitcoin
        BarcodeSchema.EMAIL -> R.string.email
        BarcodeSchema.GEO -> R.string.location
        BarcodeSchema.APP -> R.string.app
        BarcodeSchema.MMS -> R.string.mms
        BarcodeSchema.MECARD -> R.string.contact_me_card
        BarcodeSchema.PHONE -> R.string.phone
        BarcodeSchema.OTP_AUTH -> R.string.otp
        BarcodeSchema.SMS -> R.string.sms
        BarcodeSchema.URL -> R.string.url
        BarcodeSchema.VEVENT -> R.string.event
        BarcodeSchema.VCARD -> R.string.contact_v_card
        BarcodeSchema.WIFI -> R.string.wifi
        BarcodeSchema.YOUTUBE -> R.string.youtube_url
        BarcodeSchema.BOARDINGPASS -> R.string.barcode_schema_boarding_pass
        BarcodeSchema.OTHER -> R.string.text
        else -> null
    }
}