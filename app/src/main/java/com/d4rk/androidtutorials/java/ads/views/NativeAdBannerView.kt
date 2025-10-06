package com.d4rk.androidtutorials.java.ads.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.d4rk.qrcodescanner.plus.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class NativeAdBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var adView: AdView? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NativeAdBannerView, defStyleAttr, 0)
        val layoutRes = typedArray.getResourceId(R.styleable.NativeAdBannerView_nativeAdLayout, 0)
        typedArray.recycle()

        if (layoutRes != 0) {
            LayoutInflater.from(context).inflate(layoutRes, this, true)
            adView = findViewById(R.id.banner_ad_view)
        }
    }

    fun loadBannerAd(adRequest: AdRequest) {
        adView?.loadAd(adRequest)
    }

    override fun onDetachedFromWindow() {
        adView?.destroy()
        super.onDetachedFromWindow()
    }
}
