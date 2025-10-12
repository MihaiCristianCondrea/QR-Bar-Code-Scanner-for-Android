package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository

class RefreshPurchasesUseCase(private val repository: SupportRepository) {

    operator fun invoke() {
        repository.refreshPurchases()
    }
}
