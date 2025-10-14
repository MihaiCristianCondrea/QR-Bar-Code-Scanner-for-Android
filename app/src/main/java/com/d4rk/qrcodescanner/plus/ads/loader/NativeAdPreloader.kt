package com.d4rk.qrcodescanner.plus.ads.loader

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

/**
 * Helper that preloads a finite number of [NativeAd] objects so they can be handed to the
 * placement controller without triggering network calls during binding.
 */
object NativeAdPreloader {

    private const val TAG = "NativeAdPreloader"

    fun preload(
        context: Context,
        adUnitId: String,
        adRequest: AdRequest,
        count: Int,
        onFinished: (List<NativeAd>) -> Unit,
        onFailed: (LoadAdError) -> Unit = {}
    ) {
        if (count <= 0) {
            onFinished(emptyList())
            return
        }

        val loadedAds = mutableListOf<NativeAd>()
        var finished = false

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                if (finished) {
                    ad.destroy()
                    return@forNativeAd
                }
                loadedAds += ad
                if (loadedAds.size == count) {
                    finished = true
                    Log.d(TAG, "Preloaded ${loadedAds.size} native ads")
                    onFinished(ArrayList(loadedAds))
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    if (finished) return
                    finished = true
                    Log.w(TAG, "Failed to preload native ads: ${loadAdError.message}")
                    onFailed(loadAdError)
                    onFinished(ArrayList(loadedAds))
                }
            })
            .build()

        adLoader.loadAds(adRequest, count)
    }
}
