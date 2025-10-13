package com.d4rk.qrcodescanner.plus.ui.screens.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.qrcodescanner.plus.data.support.SupportRepository
import com.d4rk.qrcodescanner.plus.domain.support.InitBillingClientUseCase
import com.d4rk.qrcodescanner.plus.domain.support.InitMobileAdsUseCase
import com.d4rk.qrcodescanner.plus.domain.support.InitiatePurchaseUseCase
import com.d4rk.qrcodescanner.plus.domain.support.QueryProductDetailsUseCase
import com.d4rk.qrcodescanner.plus.domain.support.RefreshPurchasesUseCase
import com.d4rk.qrcodescanner.plus.domain.support.SetPurchaseStatusListenerUseCase
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.launch

class SupportViewModel(
    private val initBillingClientUseCase: InitBillingClientUseCase,
    private val queryProductDetailsUseCase: QueryProductDetailsUseCase,
    private val initiatePurchaseUseCase: InitiatePurchaseUseCase,
    private val initMobileAdsUseCase: InitMobileAdsUseCase,
    private val refreshPurchasesUseCase: RefreshPurchasesUseCase,
    private val setPurchaseStatusListenerUseCase: SetPurchaseStatusListenerUseCase,
) : ViewModel() {

    private val purchaseStatus = MutableLiveData<SupportPurchaseStatus>()
    private val productPrices = MutableLiveData<Map<String, String>>()
    private val uiMessage = MutableLiveData<SupportUiMessage?>()

    fun initializeSupport(productIds: List<String>) {
        viewModelScope.launch {
            val isConnected = runCatching { initBillingClientUseCase() }
                .getOrElse {
                    uiMessage.postValue(SupportUiMessage.BILLING_CONNECTION_FAILED)
                    return@launch
                }

            if (!isConnected) {
                uiMessage.postValue(SupportUiMessage.BILLING_CONNECTION_FAILED)
                return@launch
            }

            refreshPurchasesUseCase()

            val productDetails = runCatching { queryProductDetailsUseCase(productIds) }
                .getOrElse {
                    uiMessage.postValue(SupportUiMessage.PRODUCT_DETAILS_UNAVAILABLE)
                    return@launch
                }

            if (productDetails.isEmpty()) {
                uiMessage.postValue(SupportUiMessage.PRODUCT_DETAILS_UNAVAILABLE)
                return@launch
            }

            val prices = productDetails.mapNotNull { details ->
                val price = details.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
                if (price.isBlank()) {
                    null
                } else {
                    details.productId to price
                }
            }.toMap()

            if (prices.isEmpty()) {
                uiMessage.postValue(SupportUiMessage.PRODUCT_DETAILS_UNAVAILABLE)
            } else {
                productPrices.postValue(prices)
            }
        }
    }

    fun initiatePurchase(productId: String): SupportRepository.BillingFlowLauncher? {
        return initiatePurchaseUseCase(productId)
    }

    fun initMobileAds(): AdRequest {
        return initMobileAdsUseCase()
    }

    fun getPurchaseStatus(): LiveData<SupportPurchaseStatus> = purchaseStatus

    fun getProductPrices(): LiveData<Map<String, String>> = productPrices

    fun getUiMessage(): LiveData<SupportUiMessage?> = uiMessage

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
        viewModelScope.launch {
            refreshPurchasesUseCase()
        }
    }

    fun consumeUiMessage() {
        uiMessage.value = null
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

enum class SupportUiMessage {
    BILLING_CONNECTION_FAILED,
    PRODUCT_DETAILS_UNAVAILABLE,
}
