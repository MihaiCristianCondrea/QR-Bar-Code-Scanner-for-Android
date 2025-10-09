package com.d4rk.qrcodescanner.plus.utils.extension

fun Double?.orZero() : Double {
    return this ?: 0.0
}