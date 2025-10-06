package com.d4rk.androidtutorials.java.ads.loader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.d4rk.qrcodescanner.plus.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton

object NativeAdLoader {

    fun load(
        context: Context,
        container: ViewGroup,
        @LayoutRes layoutRes: Int,
        adUnitId: String,
        adRequest: AdRequest,
        listener: AdListener? = null,
        onNativeAdLoaded: (NativeAd) -> Unit
    ) {
        val adLoaderBuilder = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                val adRoot = LayoutInflater.from(context).inflate(layoutRes, container, false)
                val nativeAdView = adRoot.findViewById<NativeAdView>(R.id.native_ad_view)
                    ?: (adRoot as? NativeAdView)
                    ?: throw IllegalStateException("Native ad layout must include a NativeAdView with id native_ad_view")

                bindNativeAdView(nativeAdView, nativeAd)

                container.removeAllViews()
                if (adRoot !== nativeAdView) {
                    container.addView(adRoot)
                } else {
                    container.addView(nativeAdView)
                }

                onNativeAdLoaded(nativeAd)
            }

        listener?.let { adLoaderBuilder.withAdListener(it) }

        val adLoader = adLoaderBuilder.build()
        adLoader.loadAd(adRequest)
    }

    private fun bindNativeAdView(nativeAdView: NativeAdView, nativeAd: NativeAd) {
        val headlineView = nativeAdView.findViewById<TextView>(R.id.ad_headline)
            ?: throw IllegalStateException("Native ad layout must include a TextView with id ad_headline")
        headlineView.text = nativeAd.headline
        nativeAdView.headlineView = headlineView

        val bodyView = nativeAdView.findViewById<TextView>(R.id.ad_body)
        if (bodyView != null) {
            if (nativeAd.body.isNullOrBlank()) {
                bodyView.visibility = View.GONE
            } else {
                bodyView.visibility = View.VISIBLE
                bodyView.text = nativeAd.body
            }
            nativeAdView.bodyView = bodyView
        }

        val callToActionView = nativeAdView.findViewById<MaterialButton>(R.id.ad_call_to_action)
        if (callToActionView != null) {
            if (nativeAd.callToAction.isNullOrBlank()) {
                callToActionView.visibility = View.GONE
            } else {
                callToActionView.visibility = View.VISIBLE
                callToActionView.text = nativeAd.callToAction
            }
            nativeAdView.callToActionView = callToActionView
        }

        val iconView = nativeAdView.findViewById<ImageView>(R.id.ad_app_icon)
        if (iconView != null) {
            val icon = nativeAd.icon
            if (icon == null) {
                iconView.setImageDrawable(null)
                iconView.visibility = View.GONE
            } else {
                iconView.setImageDrawable(icon.drawable)
                iconView.visibility = View.VISIBLE
            }
            nativeAdView.iconView = iconView
        }

        nativeAdView.setNativeAd(nativeAd)
    }
}
