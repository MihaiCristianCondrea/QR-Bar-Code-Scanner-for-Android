package com.d4rk.qrcodescanner.plus.extension

fun Boolean?.orFalse() : Boolean {
    return this == true
}