package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository

class QueryProductDetailsUseCase(private val repository: SupportRepository) {

    operator fun invoke(
        productIds: List<String>,
        listener: SupportRepository.OnProductDetailsListener,
    ) {
        repository.queryProductDetails(productIds, listener)
    }
}
