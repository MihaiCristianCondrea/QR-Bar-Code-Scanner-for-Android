package com.d4rk.qrcodescanner.plus.utils.extension

fun Long?.orZero() : Long {
    return this ?: 0L
}