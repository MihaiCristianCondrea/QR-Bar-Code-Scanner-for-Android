package com.d4rk.qrcodescanner.plus.ads.loader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.d4rk.qrcodescanner.plus.R
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

internal object NativeAdViewBinder {

    fun bind(
        context: Context,
        container: ViewGroup,
        @LayoutRes layoutRes: Int,
        nativeAd: NativeAd
    ) {
        val inflater = LayoutInflater.from(context)
        val adView = inflater.inflate(layoutRes, container, false) as NativeAdView
        adView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        adView.setPadding(
            container.paddingLeft,
            container.paddingTop,
            container.paddingRight,
            container.paddingBottom
        )
        container.setPadding(0, 0, 0, 0)
        populateNativeAdView(nativeAd, adView)
        container.removeAllViews()
        container.addView(adView)
        container.visibility = View.VISIBLE
        container.requestLayout()
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaCard = adView.findViewById<View?>(R.id.media_card)
        val mediaView = adView.findViewById<MediaView?>(R.id.ad_media)
        val headlineView = adView.findViewById<TextView?>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView?>(R.id.ad_body)
        val callToActionView = adView.findViewById<Button?>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView?>(R.id.ad_app_icon)
        val attributionView = adView.findViewById<TextView?>(R.id.ad_attribution)
        val adChoicesView = adView.findViewById<AdChoicesView?>(R.id.ad_choices)

        mediaView?.let { adView.mediaView = it }
        headlineView?.let {
            it.text = nativeAd.headline
            adView.headlineView = it
        }

        bodyView?.let {
            if (nativeAd.body.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                it.text = nativeAd.body
            }
            adView.bodyView = it
        }

        callToActionView?.let {
            if (nativeAd.callToAction.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                it.text = nativeAd.callToAction
            }
            adView.callToActionView = it
        }

        iconView?.let { view ->
            val icon = nativeAd.icon
            if (icon == null) {
                view.visibility = View.GONE
                view.setImageDrawable(null)
            } else {
                view.visibility = View.VISIBLE
                view.setImageDrawable(icon.drawable)
            }
            adView.iconView = view
        }

        attributionView?.let {
            val adLabel = adView.context.getString(R.string.ad_attribution)
            it.text = if (nativeAd.advertiser.isNullOrEmpty()) {
                adLabel
            } else {
                "$adLabel ${nativeAd.advertiser}"
            }
            adView.advertiserView = it
        }

        adChoicesView?.let {
            it.visibility = View.VISIBLE
            adView.adChoicesView = it
        }

        val mediaContent = nativeAd.mediaContent
        if (mediaView != null && mediaContent != null) {
            mediaView.visibility = View.VISIBLE
            mediaView.mediaContent = mediaContent
            mediaCard?.visibility = View.VISIBLE
        } else {
            mediaView?.visibility = View.GONE
            mediaCard?.visibility = View.GONE
        }

        adView.setNativeAd(nativeAd)
    }
}
