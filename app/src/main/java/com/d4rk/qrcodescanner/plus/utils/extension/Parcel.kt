package com.d4rk.qrcodescanner.plus.utils.extension

import android.os.Parcel

fun Parcel?.writeBool(value: Boolean) { // FIXME: Function "writeBool" is never used
    val newValue = if (value) 1 else 0
    this?.writeInt(newValue)
}

fun Parcel?.readBool(): Boolean { // FIXME: Function "readBool" is never used
    return when (this?.readInt()) {
        1 -> true
        else -> false
    }
}