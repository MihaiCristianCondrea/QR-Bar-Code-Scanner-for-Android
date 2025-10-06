package com.d4rk.qrcodescanner.plus.extension

fun Int?.orZero() : Int {
    return this ?: 0
}