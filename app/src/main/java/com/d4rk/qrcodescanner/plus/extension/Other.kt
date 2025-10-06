package com.d4rk.qrcodescanner.plus.extension

fun <T> unsafeLazy(initializer : () -> T) : Lazy<T> = lazy(LazyThreadSafetyMode.NONE , initializer)