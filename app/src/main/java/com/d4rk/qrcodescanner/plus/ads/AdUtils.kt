package com.d4rk.qrcodescanner.plus.ads

import com.d4rk.qrcodescanner.plus.ads.views.NativeAdBannerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest

object AdUtils {

    fun loadBanner(nativeAdView : NativeAdBannerView , listener : AdListener? = null) {
        val request = AdRequest.Builder().build()
        nativeAdView.loadAd(request , listener)
    }
}
