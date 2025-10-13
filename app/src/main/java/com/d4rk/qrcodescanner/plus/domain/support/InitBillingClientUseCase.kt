package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class InitBillingClientUseCase(private val repository: SupportRepository) {

    suspend operator fun invoke(): Boolean = suspendCancellableCoroutine { continuation ->
        repository.initBillingClient { isConnected ->
            if (continuation.isActive) {
                continuation.resume(isConnected)
            }
        }
    }
}
