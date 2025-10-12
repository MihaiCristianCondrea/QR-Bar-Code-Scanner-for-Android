package com.d4rk.qrcodescanner.plus.ui.screens.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.d4rk.qrcodescanner.plus.data.support.SupportRepository
import com.d4rk.qrcodescanner.plus.domain.support.InitBillingClientUseCase
import com.d4rk.qrcodescanner.plus.domain.support.InitMobileAdsUseCase
import com.d4rk.qrcodescanner.plus.domain.support.InitiatePurchaseUseCase
import com.d4rk.qrcodescanner.plus.domain.support.QueryProductDetailsUseCase
import com.d4rk.qrcodescanner.plus.domain.support.RefreshPurchasesUseCase
import com.d4rk.qrcodescanner.plus.domain.support.SetPurchaseStatusListenerUseCase
import com.google.android.gms.ads.AdRequest

class SupportViewModel(
    private val initBillingClientUseCase: InitBillingClientUseCase,
    private val queryProductDetailsUseCase: QueryProductDetailsUseCase,
    private val initiatePurchaseUseCase: InitiatePurchaseUseCase,
    private val initMobileAdsUseCase: InitMobileAdsUseCase,
    private val refreshPurchasesUseCase: RefreshPurchasesUseCase,
    private val setPurchaseStatusListenerUseCase: SetPurchaseStatusListenerUseCase,
) : ViewModel() {

    private val purchaseStatus = MutableLiveData<SupportPurchaseStatus>()

    fun initBillingClient(onConnected: (() -> Unit)? = null) {
        initBillingClientUseCase {
            refreshPurchasesUseCase()
            onConnected?.invoke()
        }
    }

    fun queryProductDetails(
        productIds: List<String>,
        listener: SupportRepository.OnProductDetailsListener,
    ) {
        queryProductDetailsUseCase(productIds, listener)
    }

    fun initiatePurchase(productId: String): SupportRepository.BillingFlowLauncher? {
        return initiatePurchaseUseCase(productId)
    }

    fun initMobileAds(): AdRequest {
        return initMobileAdsUseCase()
    }

    fun getPurchaseStatus(): LiveData<SupportPurchaseStatus> = purchaseStatus

    fun registerPurchaseStatusListener() {
        setPurchaseStatusListenerUseCase(
            object : SupportRepository.PurchaseStatusListener {
                override fun onPurchaseAcknowledged(productId: String, isNewPurchase: Boolean) {
                    purchaseStatus.postValue(
                        SupportPurchaseStatus(
                            productId = productId,
                            state = SupportPurchaseStatus.State.GRANTED,
                            isNewPurchase = isNewPurchase,
                        ),
                    )
                }

                override fun onPurchaseRevoked(productId: String) {
                    purchaseStatus.postValue(
                        SupportPurchaseStatus(
                            productId = productId,
                            state = SupportPurchaseStatus.State.REVOKED,
                            isNewPurchase = false,
                        ),
                    )
                }
            },
        )
    }

    fun refreshPurchases() {
        refreshPurchasesUseCase()
    }
}

class SupportViewModelFactory(
    private val initBillingClientUseCase: InitBillingClientUseCase,
    private val queryProductDetailsUseCase: QueryProductDetailsUseCase,
    private val initiatePurchaseUseCase: InitiatePurchaseUseCase,
    private val initMobileAdsUseCase: InitMobileAdsUseCase,
    private val refreshPurchasesUseCase: RefreshPurchasesUseCase,
    private val setPurchaseStatusListenerUseCase: SetPurchaseStatusListenerUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SupportViewModel(
                initBillingClientUseCase = initBillingClientUseCase,
                queryProductDetailsUseCase = queryProductDetailsUseCase,
                initiatePurchaseUseCase = initiatePurchaseUseCase,
                initMobileAdsUseCase = initMobileAdsUseCase,
                refreshPurchasesUseCase = refreshPurchasesUseCase,
                setPurchaseStatusListenerUseCase = setPurchaseStatusListenerUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
