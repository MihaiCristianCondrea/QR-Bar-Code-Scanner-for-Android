package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository

class SetPurchaseStatusListenerUseCase(private val repository: SupportRepository) {

    operator fun invoke(listener: SupportRepository.PurchaseStatusListener) {
        repository.setPurchaseStatusListener(listener)
    }
}
