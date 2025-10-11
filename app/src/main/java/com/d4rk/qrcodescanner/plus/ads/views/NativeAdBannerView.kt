package com.d4rk.qrcodescanner.plus.ads.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.res.use
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.loader.NativeAdLoader
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd

class NativeAdBannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @LayoutRes
    private var layoutRes: Int = R.layout.ad_bottom_app_bar
    private var adUnitId: String = context.getString(R.string.native_ad_fallback_unit_id)
    private var nativeAd: NativeAd? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.NativeAdBannerView, defStyleAttr, 0)
            .use { typedArray ->
                layoutRes = typedArray.getResourceId(
                    R.styleable.NativeAdBannerView_nativeAdLayout,
                    layoutRes
                )
                val adUnitValue =
                    typedArray.getString(R.styleable.NativeAdBannerView_nativeAdUnitId)
                if (!adUnitValue.isNullOrBlank()) {
                    adUnitId = adUnitValue
                }
            }
    }

    fun loadAd() {
        loadAd(AdRequest.Builder().build(), null)
    }

    fun loadAd(listener: AdListener?) {
        loadAd(AdRequest.Builder().build(), listener)
    }

    fun loadAd(request: AdRequest) {
        loadAd(request, null)
    }

    fun loadAd(request: AdRequest, listener: AdListener?) {
        NativeAdLoader.load(
            context = context,
            container = this,
            layoutRes = layoutRes,
            adRequest = request,
            listener = listener,
            adUnitId = adUnitId,
            onNativeAdLoaded = { loadedAd ->
                nativeAd?.destroy()
                nativeAd = loadedAd
            }
        )
    }

    fun setNativeAdLayout(@LayoutRes layoutRes: Int) {
        this.layoutRes = layoutRes
    }

    fun setNativeAdUnitId(adUnitId: String?) {
        nativeAd?.destroy()
        nativeAd = null
        this.adUnitId = adUnitId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.native_ad_fallback_unit_id)
    }

    fun setNativeAdUnitId(@StringRes adUnitIdRes: Int) {
        setNativeAdUnitId(context.getString(adUnitIdRes))
    }

    override fun onDetachedFromWindow() {
        nativeAd?.destroy()
        nativeAd = null
        super.onDetachedFromWindow()
    }
}
