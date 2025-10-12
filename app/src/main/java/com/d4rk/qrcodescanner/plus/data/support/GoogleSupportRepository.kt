package com.d4rk.qrcodescanner.plus.data.support

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class GoogleSupportRepository(context: Context) : SupportRepository {

    private val applicationContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val productDetailsCache = ConcurrentHashMap<String, ProductDetails>()
    private val grantedProductIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val pendingAcknowledgeTokens = Collections.synchronizedSet(mutableSetOf<String>())
    private val pendingConnectionCallbacks = CopyOnWriteArrayList<() -> Unit>()
    private val isConnecting = AtomicBoolean(false)
    private val adsInitialized = AtomicBoolean(false)
    @Volatile
    private var purchaseStatusListener: SupportRepository.PurchaseStatusListener? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(applicationContext)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases, trackRevocations = false)
            } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.w(TAG, "onPurchasesUpdated: ${billingResult.debugMessage}")
            }
        }
        .enablePendingPurchases()
        .build()

    override fun initBillingClient(onConnected: (() -> Unit)?) {
        if (billingClient.isReady) {
            onConnected?.let { callback -> mainHandler.post(callback) }
            return
        }

        onConnected?.let(pendingConnectionCallbacks::add)

        if (isConnecting.compareAndSet(false, true).not()) {
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting.set(false)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    pendingConnectionCallbacks.forEach { callback -> mainHandler.post(callback) }
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
                pendingConnectionCallbacks.clear()
            }

            override fun onBillingServiceDisconnected() {
                isConnecting.set(false)
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    override fun queryProductDetails(
        productIds: List<String>,
        listener: SupportRepository.OnProductDetailsListener,
    ) {
        if (billingClient.isReady.not()) {
            initBillingClient { queryProductDetails(productIds, listener) }
            return
        }

        val paramsList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(paramsList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsCache.clear()
                productDetailsList.forEach { details ->
                    productDetailsCache[details.productId] = details
                }
                listener.onProductDetailsRetrieved(productDetailsList)
            } else {
                Log.w(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                listener.onProductDetailsRetrieved(emptyList())
            }
        }
    }

    override fun initiatePurchase(productId: String): SupportRepository.BillingFlowLauncher? {
        if (billingClient.isReady.not()) {
            Log.w(TAG, "Billing client not ready, cannot launch purchase flow")
            return null
        }

        val productDetails = productDetailsCache[productId]
        if (productDetails == null) {
            Log.w(TAG, "No ProductDetails cached for productId=$productId")
            return null
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        return SupportRepository.BillingFlowLauncher { activity ->
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, params)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
            }
        }
    }

    override fun initMobileAds(): AdRequest {
        if (adsInitialized.compareAndSet(false, true)) {
            MobileAds.initialize(applicationContext)
        }
        return AdRequest.Builder().build()
    }

    override fun setPurchaseStatusListener(listener: SupportRepository.PurchaseStatusListener) {
        purchaseStatusListener = listener
    }

    override fun refreshPurchases() {
        if (billingClient.isReady.not()) {
            initBillingClient { refreshPurchases() }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases, trackRevocations = true)
            } else {
                Log.w(TAG, "Failed to refresh purchases: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>, trackRevocations: Boolean) {
        val activeProductIds = mutableSetOf<String>()
        purchases.filter { purchase -> purchase.purchaseState == Purchase.PurchaseState.PURCHASED }
            .forEach { purchase ->
                activeProductIds.addAll(purchase.products)
                if (purchase.isAcknowledged) {
                    grantProducts(purchase.products, isNewPurchase = false)
                } else {
                    acknowledgePurchase(purchase)
                }
            }

        if (trackRevocations) {
            val revoked = grantedProductIds.toList().filterNot(activeProductIds::contains)
            revoked.forEach { productId ->
                grantedProductIds.remove(productId)
                dispatchPurchaseRevoked(productId)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!pendingAcknowledgeTokens.add(purchase.purchaseToken)) {
            return
        }

        val productIds = purchase.products.toList()
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            pendingAcknowledgeTokens.remove(purchase.purchaseToken)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                grantProducts(productIds, isNewPurchase = true)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                productIds.forEach { productId ->
                    grantedProductIds.remove(productId)
                    dispatchPurchaseRevoked(productId)
                }
            } else {
                Log.w(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    private fun grantProducts(productIds: List<String>, isNewPurchase: Boolean) {
        productIds.forEach { productId ->
            if (grantedProductIds.add(productId)) {
                dispatchPurchaseAcknowledged(productId, isNewPurchase)
            }
        }
    }

    private fun dispatchPurchaseAcknowledged(productId: String, isNewPurchase: Boolean) {
        mainHandler.post {
            purchaseStatusListener?.onPurchaseAcknowledged(productId, isNewPurchase)
        }
    }

    private fun dispatchPurchaseRevoked(productId: String) {
        mainHandler.post {
            purchaseStatusListener?.onPurchaseRevoked(productId)
        }
    }

    companion object {
        private const val TAG = "GoogleSupportRepo"
    }
}
