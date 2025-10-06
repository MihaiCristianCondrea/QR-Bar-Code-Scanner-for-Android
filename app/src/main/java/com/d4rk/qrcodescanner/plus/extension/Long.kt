package com.d4rk.qrcodescanner.plus.extension

fun Long?.orZero() : Long {
    return this ?: 0L
}