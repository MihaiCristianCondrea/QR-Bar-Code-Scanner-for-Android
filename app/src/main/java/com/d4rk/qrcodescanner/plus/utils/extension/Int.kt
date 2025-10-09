package com.d4rk.qrcodescanner.plus.utils.extension

fun Int?.orZero() : Int {
    return this ?: 0
}