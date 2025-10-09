package com.d4rk.qrcodescanner.plus.utils.extension

import com.google.mlkit.vision.barcode.common.Barcode as MlKitBarcode
import com.google.zxing.BarcodeFormat

fun BarcodeFormat.toGmsFormat() : Int? {
    return when (this) {
        BarcodeFormat.AZTEC -> MlKitBarcode.FORMAT_AZTEC
        BarcodeFormat.CODABAR -> MlKitBarcode.FORMAT_CODABAR
        BarcodeFormat.CODE_39 -> MlKitBarcode.FORMAT_CODE_39
        BarcodeFormat.CODE_93 -> MlKitBarcode.FORMAT_CODE_93
        BarcodeFormat.CODE_128 -> MlKitBarcode.FORMAT_CODE_128
        BarcodeFormat.DATA_MATRIX -> MlKitBarcode.FORMAT_DATA_MATRIX
        BarcodeFormat.EAN_8 -> MlKitBarcode.FORMAT_EAN_8
        BarcodeFormat.EAN_13 -> MlKitBarcode.FORMAT_EAN_13
        BarcodeFormat.ITF -> MlKitBarcode.FORMAT_ITF
        BarcodeFormat.PDF_417 -> MlKitBarcode.FORMAT_PDF417
        BarcodeFormat.QR_CODE -> MlKitBarcode.FORMAT_QR_CODE
        BarcodeFormat.UPC_A -> MlKitBarcode.FORMAT_UPC_A
        BarcodeFormat.UPC_E -> MlKitBarcode.FORMAT_UPC_E
        else -> null
    }
}

fun Int.toZxingFormat() : BarcodeFormat? {
    return when (this) {
        MlKitBarcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        MlKitBarcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        MlKitBarcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        MlKitBarcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        MlKitBarcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        MlKitBarcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        MlKitBarcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        MlKitBarcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        MlKitBarcode.FORMAT_ITF -> BarcodeFormat.ITF
        MlKitBarcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
        MlKitBarcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        MlKitBarcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        MlKitBarcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        else -> null
    }
}
