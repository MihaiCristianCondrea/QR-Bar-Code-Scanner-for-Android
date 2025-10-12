package com.d4rk.qrcodescanner.plus.ui.screens.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivitySupportBinding
import com.d4rk.qrcodescanner.plus.di.initBillingClientUseCase
import com.d4rk.qrcodescanner.plus.di.initMobileAdsUseCase
import com.d4rk.qrcodescanner.plus.di.initiatePurchaseUseCase
import com.d4rk.qrcodescanner.plus.di.queryProductDetailsUseCase
import com.d4rk.qrcodescanner.plus.di.refreshPurchasesUseCase
import com.d4rk.qrcodescanner.plus.di.setPurchaseStatusListenerUseCase
import com.d4rk.qrcodescanner.plus.ui.components.navigation.UpNavigationActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper

class SupportActivity : UpNavigationActivity() {

    private lateinit var binding: ActivitySupportBinding

    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory(
            initBillingClientUseCase = initBillingClientUseCase,
            queryProductDetailsUseCase = queryProductDetailsUseCase,
            initiatePurchaseUseCase = initiatePurchaseUseCase,
            initMobileAdsUseCase = initMobileAdsUseCase,
            refreshPurchasesUseCase = refreshPurchasesUseCase,
            setPurchaseStatusListenerUseCase = setPurchaseStatusListenerUseCase,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window, binding.root)
        setContentView(binding.root)

        supportViewModel.registerPurchaseStatusListener()
        supportViewModel.getPurchaseStatus().observe(this, this::handlePurchaseStatus)

        setupDonationButtons()
        setupSupportLink()
        loadAds()

        supportViewModel.initBillingClient { queryProductDetails() }
    }

    override fun onResume() {
        super.onResume()
        binding.bannerAdView.resume()
        supportViewModel.refreshPurchases()
    }

    override fun onPause() {
        binding.bannerAdView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.bannerAdView.destroy()
        super.onDestroy()
    }

    private fun setupDonationButtons() {
        binding.buttonLowDonation.setText(R.string.support_low_donation)
        binding.buttonNormalDonation.setText(R.string.support_normal_donation)
        binding.buttonHighDonation.setText(R.string.support_high_donation)
        binding.buttonExtremeDonation.setText(R.string.support_extreme_donation)

        binding.buttonLowDonation.setOnClickListener { initiatePurchase(LOW_DONATION_ID) }
        binding.buttonNormalDonation.setOnClickListener { initiatePurchase(NORMAL_DONATION_ID) }
        binding.buttonHighDonation.setOnClickListener { initiatePurchase(HIGH_DONATION_ID) }
        binding.buttonExtremeDonation.setOnClickListener { initiatePurchase(EXTREME_DONATION_ID) }
    }

    private fun setupSupportLink() {
        binding.buttonWebAd.setOnClickListener { openSupportLink() }
    }

    private fun loadAds() {
        val adRequest = supportViewModel.initMobileAds()
        binding.supportNativeAd.loadAd(adRequest)
        binding.bannerAdView.loadAd(adRequest)
    }

    private fun queryProductDetails() {
        val productIds = listOf(
            LOW_DONATION_ID,
            NORMAL_DONATION_ID,
            HIGH_DONATION_ID,
            EXTREME_DONATION_ID,
        )

        supportViewModel.queryProductDetails(productIds) { productDetailsList ->
            productDetailsList.forEach { productDetails ->
                val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
                if (price.isBlank()) return@forEach
                when (productDetails.productId) {
                    LOW_DONATION_ID -> binding.buttonLowDonation.text = price
                    NORMAL_DONATION_ID -> binding.buttonNormalDonation.text = price
                    HIGH_DONATION_ID -> binding.buttonHighDonation.text = price
                    EXTREME_DONATION_ID -> binding.buttonExtremeDonation.text = price
                }
            }
        }
    }

    private fun initiatePurchase(productId: String) {
        val launcher = supportViewModel.initiatePurchase(productId)
        if (launcher == null) {
            Toast.makeText(this, R.string.support_purchase_error, Toast.LENGTH_LONG).show()
            return
        }
        launcher.launch(this)
    }

    private fun openSupportLink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.support_link_url)))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.support_link_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    private fun handlePurchaseStatus(status: SupportPurchaseStatus?) {
        status ?: return
        val messageRes = when (status.state) {
            SupportPurchaseStatus.State.GRANTED -> if (status.isNewPurchase) {
                R.string.support_purchase_thank_you
            } else {
                R.string.support_purchase_restored
            }

            SupportPurchaseStatus.State.REVOKED -> R.string.support_purchase_revoked
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val LOW_DONATION_ID = "low_donation"
        private const val NORMAL_DONATION_ID = "normal_donation"
        private const val HIGH_DONATION_ID = "high_donation"
        private const val EXTREME_DONATION_ID = "extreme_donation"
    }
}
