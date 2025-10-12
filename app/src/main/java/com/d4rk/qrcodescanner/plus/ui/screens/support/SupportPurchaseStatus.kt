package com.d4rk.qrcodescanner.plus.ui.screens.support

data class SupportPurchaseStatus(
    val productId: String,
    val state: State,
    val isNewPurchase: Boolean,
) {

    enum class State {
        GRANTED,
        REVOKED,
    }
}
