package com.d4rk.qrcodescanner.plus.utils.extension

import com.d4rk.qrcodescanner.plus.model.Barcode
import com.google.zxing.Result

fun Result.equalTo(barcode: Barcode?): Boolean { // FIXME: Function "equalTo" is never used
    return barcodeFormat == barcode?.format && text == barcode.text
}