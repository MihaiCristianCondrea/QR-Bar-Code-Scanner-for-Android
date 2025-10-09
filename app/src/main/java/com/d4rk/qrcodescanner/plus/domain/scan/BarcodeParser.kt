package com.d4rk.qrcodescanner.plus.domain.scan

import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.model.schema.App
import com.d4rk.qrcodescanner.plus.model.schema.BoardingPass
import com.d4rk.qrcodescanner.plus.model.schema.Bookmark
import com.d4rk.qrcodescanner.plus.model.schema.Cryptocurrency
import com.d4rk.qrcodescanner.plus.model.schema.Email
import com.d4rk.qrcodescanner.plus.model.schema.Geo
import com.d4rk.qrcodescanner.plus.model.schema.GoogleMaps
import com.d4rk.qrcodescanner.plus.model.schema.MeCard
import com.d4rk.qrcodescanner.plus.model.schema.Mms
import com.d4rk.qrcodescanner.plus.model.schema.NZCovidTracer
import com.d4rk.qrcodescanner.plus.model.schema.Other
import com.d4rk.qrcodescanner.plus.model.schema.OtpAuth
import com.d4rk.qrcodescanner.plus.model.schema.Phone
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.model.schema.Sms
import com.d4rk.qrcodescanner.plus.model.schema.Url
import com.d4rk.qrcodescanner.plus.model.schema.VCard
import com.d4rk.qrcodescanner.plus.model.schema.VEvent
import com.d4rk.qrcodescanner.plus.model.schema.Wifi
import com.d4rk.qrcodescanner.plus.model.schema.Youtube
import com.d4rk.qrcodescanner.plus.utils.extension.toZxingFormat
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.google.mlkit.vision.barcode.common.Barcode as MlKitBarcode

object BarcodeParser {
    fun parseResult(result : Result) : Barcode {
        return parse(
            format = result.barcodeFormat ,
            text = result.text ,
            timestamp = result.timestamp ,
            errorCorrectionLevel = result.resultMetadata?.get(ResultMetadataType.ERROR_CORRECTION_LEVEL) as? String ,
            country = result.resultMetadata?.get(ResultMetadataType.POSSIBLE_COUNTRY) as? String
        )
    }

    fun parse(barcode : MlKitBarcode) : Barcode? {
        val rawValue = barcode.rawValue ?: return null
        val format = barcode.format.toZxingFormat() ?: return null
        return parse(format = format , text = rawValue)
    }

    fun parse(
        format : BarcodeFormat ,
        text : String ,
        timestamp : Long = System.currentTimeMillis() ,
        errorCorrectionLevel : String? = null ,
        country : String? = null ,
    ) : Barcode {
        val schema = parseSchema(format , text)
        return Barcode(
            text = text ,
            formattedText = schema.toFormattedText() ,
            format = format ,
            schema = schema.schema ,
            date = timestamp ,
            errorCorrectionLevel = errorCorrectionLevel ,
            country = country
        )
    }

    fun parseSchema(format : BarcodeFormat , text : String) : Schema {
        if (format != BarcodeFormat.QR_CODE) {
            return BoardingPass.parse(text) ?: Other(text)
        }
        return App.parse(text) ?: Youtube.parse(text) ?: GoogleMaps.parse(text) ?: Url.parse(text) ?: Phone.parse(text) ?: Geo.parse(text) ?: Bookmark.parse(text) ?: Sms.parse(text) ?: Mms.parse(text) ?: Wifi.parse(text) ?: Email.parse(text) ?: Cryptocurrency.parse(text) ?: VEvent.parse(text)
        ?: MeCard.parse(text) ?: VCard.parse(text) ?: OtpAuth.parse(text) ?: NZCovidTracer.parse(text) ?: BoardingPass.parse(text) ?: Other(text)
    }
}