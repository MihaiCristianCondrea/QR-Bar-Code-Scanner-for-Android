package com.d4rk.qrcodescanner.plus.extension

import java.util.Locale

val Locale?.isRussian : Boolean get() = this?.language == "ru"