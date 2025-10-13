package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class QueryProductDetailsUseCase(private val repository: SupportRepository) {

    suspend operator fun invoke(productIds: List<String>): List<ProductDetails> =
        suspendCancellableCoroutine { continuation ->
            repository.queryProductDetails(productIds) { productDetailsList ->
                if (continuation.isActive) {
                    continuation.resume(productDetailsList)
                }
            }
        }
    }
}
