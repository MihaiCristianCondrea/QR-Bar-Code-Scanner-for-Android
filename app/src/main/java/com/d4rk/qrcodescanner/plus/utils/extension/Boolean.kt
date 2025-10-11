package com.d4rk.qrcodescanner.plus.utils.extension

fun Boolean?.orFalse(): Boolean {
    return this == true
}