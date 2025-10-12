package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository

class InitBillingClientUseCase(private val repository: SupportRepository) {

    operator fun invoke(onConnected: (() -> Unit)? = null) {
        repository.initBillingClient(onConnected)
    }
}
