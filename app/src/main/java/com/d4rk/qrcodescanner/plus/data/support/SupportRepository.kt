package com.d4rk.qrcodescanner.plus.data.support

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.google.android.gms.ads.AdRequest

/**
 * Repository that coordinates Google Play Billing and Ads functionality for the support screen.
 */
interface SupportRepository {

    fun initBillingClient(onConnected: (() -> Unit)? = null)

    fun queryProductDetails(
        productIds: List<String>,
        listener: OnProductDetailsListener,
    )

    fun initiatePurchase(productId: String): BillingFlowLauncher?

    fun initMobileAds(): AdRequest

    fun setPurchaseStatusListener(listener: PurchaseStatusListener)

    fun refreshPurchases()

    fun interface OnProductDetailsListener {
        fun onProductDetailsRetrieved(productDetailsList: List<ProductDetails>)
    }

    fun interface BillingFlowLauncher {
        fun launch(activity: Activity)
    }

    interface PurchaseStatusListener {
        fun onPurchaseAcknowledged(productId: String, isNewPurchase: Boolean)

        fun onPurchaseRevoked(productId: String)
    }
}
