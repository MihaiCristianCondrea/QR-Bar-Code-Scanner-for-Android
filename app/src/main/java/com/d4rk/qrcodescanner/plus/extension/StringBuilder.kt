package com.d4rk.qrcodescanner.plus.extension

fun StringBuilder.appendIfNotNullOrBlank(prefix : String = "" , value : String? , suffix : String = "") : StringBuilder {
    if (value.isNullOrBlank().not()) {
        append(prefix)
        append(value)
        append(suffix)
    }
    return this
}