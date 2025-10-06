package com.d4rk.qrcodescanner.plus.extension

import com.d4rk.qrcodescanner.plus.R
import com.google.zxing.BarcodeFormat

fun BarcodeFormat.toStringId() : Int {
    return when (this) {
        BarcodeFormat.AZTEC -> R.string.aztec
        BarcodeFormat.CODABAR -> R.string.codabar
        BarcodeFormat.CODE_39 -> R.string.code_39
        BarcodeFormat.CODE_93 -> R.string.code_93
        BarcodeFormat.CODE_128 -> R.string.code_128
        BarcodeFormat.DATA_MATRIX -> R.string.data_matrix
        BarcodeFormat.EAN_8 -> R.string.ean_8
        BarcodeFormat.EAN_13 -> R.string.ean_13
        BarcodeFormat.ITF -> R.string.itf
        BarcodeFormat.PDF_417 -> R.string.pdf_417
        BarcodeFormat.QR_CODE -> R.string.qr_code
        BarcodeFormat.UPC_A -> R.string.upc_a
        BarcodeFormat.UPC_E -> R.string.upc_e
        else -> R.string.qr_code
    }
}

fun BarcodeFormat.toImageId() : Int {
    return when (this) {
        BarcodeFormat.QR_CODE -> R.drawable.ic_qr_code_white
        BarcodeFormat.DATA_MATRIX -> R.drawable.ic_data_matrix
        BarcodeFormat.AZTEC -> R.drawable.ic_aztec
        BarcodeFormat.PDF_417 -> R.drawable.ic_pdf417
        else -> R.drawable.ic_barcode
    }
}