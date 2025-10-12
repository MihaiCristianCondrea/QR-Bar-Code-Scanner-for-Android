package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository

class InitiatePurchaseUseCase(private val repository: SupportRepository) {

    operator fun invoke(productId: String): SupportRepository.BillingFlowLauncher? {
        return repository.initiatePurchase(productId)
    }
}
