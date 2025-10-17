package com.d4rk.qrcodescanner.plus.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.data.onboarding.OnboardingPreferences
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferencesRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AppOpenAdManager(
    application : Application ,
    private val preferencesRepository : UsageAndDiagnosticsPreferencesRepository ,
    scope : CoroutineScope ,
    private val timeProvider : () -> Long = System::currentTimeMillis ,
) {

    fun interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private val applicationContext = application.applicationContext
    private val adUnitId : String = if (BuildConfig.DEBUG) {
        TEST_APP_OPEN_AD_UNIT_ID
    }
    else {
        applicationContext.getString(R.string.app_open_ad_unit_id)
    }

    private var appOpenAd : AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd : Boolean = false
        private set
    private var loadTime : Long = 0L
    private var consentAllowsAds : Boolean = false
    private var mobileAdsInitialized = false

    init {
        scope.launch {
            preferencesRepository.observePreferences().map { preferences ->
                        preferences.usageAndDiagnosticsEnabled && preferences.adStorageConsentGranted
                    }.distinctUntilChanged().collectLatest { canRequest ->
                        consentAllowsAds = canRequest
                        if (canRequest && canRequestAds()) {
                            initializeMobileAdsIfNeeded()
                            loadAd(applicationContext)
                        }
                        else if (! canRequest) {
                            appOpenAd = null
                        }
                    }
        }
    }

    fun loadAd(context : Context) {
        if (! canRequestAds()) {
            Log.d(LOG_TAG , "Cannot load app open ad yet. Consent or onboarding incomplete.")
            return
        }

        if (isLoadingAd || isAdAvailable()) {
            return
        }

        initializeMobileAdsIfNeeded()
        isLoadingAd = true

        val loadContext = context.applicationContext

        AppOpenAd.load(
            loadContext ,
            adUnitId ,
            AdRequest.Builder().build() ,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad : AppOpenAd) {
                    Log.d(LOG_TAG , "App open ad loaded.")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = timeProvider()
                }

                override fun onAdFailedToLoad(loadAdError : LoadAdError) {
                    Log.d(
                        LOG_TAG ,
                        "App open ad failed to load with error: ${loadAdError.message}" ,
                    )
                    isLoadingAd = false
                }
            } ,
        )
    }

    fun showAdIfAvailable(activity : Activity) {
        showAdIfAvailable(activity) { }
    }

    fun showAdIfAvailable(
        activity : Activity ,
        onShowAdCompleteListener : OnShowAdCompleteListener ,
    ) {
        if (! canRequestAds()) {
            Log.d(LOG_TAG , "Cannot show app open ad yet. Consent or onboarding incomplete.")
            onShowAdCompleteListener.onShowAdComplete()
            return
        }

        if (isShowingAd) {
            Log.d(LOG_TAG , "The app open ad is already showing.")
            return
        }

        val ad = appOpenAd
        if (ad == null) {
            Log.d(LOG_TAG , "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity.applicationContext)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG , "onAdDismissedFullScreenContent.")
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError : AdError) {
                Log.d(
                    LOG_TAG ,
                    "onAdFailedToShowFullScreenContent: ${adError.message}" ,
                )
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(applicationContext)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(LOG_TAG , "onAdShowedFullScreenContent.")
            }

            override fun onAdImpression() {
                Log.d(LOG_TAG , "onAdImpression.")
            }

            override fun onAdClicked() {
                Log.d(LOG_TAG , "onAdClicked.")
            }
        }

        isShowingAd = true
        ad.show(activity)
    }

    private fun canRequestAds() : Boolean {
        return consentAllowsAds && OnboardingPreferences.isOnboardingComplete(applicationContext)
    }

    private fun initializeMobileAdsIfNeeded() {
        if (! mobileAdsInitialized) {
            MobileAds.initialize(applicationContext)
            mobileAdsInitialized = true
        }
    }

    private fun isAdAvailable() : Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(AD_EXPIRATION_HOURS)
    }

    private fun wasLoadTimeLessThanNHoursAgo(@Suppress("SameParameterValue") numHours : Long) : Boolean {
        val timeDifference = timeProvider() - loadTime
        val millisecondsPerHour = TimeUnit.HOURS.toMillis(1)
        return timeDifference < millisecondsPerHour * numHours
    }

    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
        private const val AD_EXPIRATION_HOURS = 4L
        private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }
}
