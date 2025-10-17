package com.d4rk.qrcodescanner.plus

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.d4rk.qrcodescanner.plus.ads.AppOpenAdManager
import com.d4rk.qrcodescanner.plus.di.appModule
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class QrCodeScanner :
    Application(),
    Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super<Application>.onCreate()
        startKoin {
            androidContext(this@QrCodeScanner)
            modules(appModule)
        }

        val usageAndDiagnosticsRepository: UsageAndDiagnosticsPreferencesRepository =
            getKoin().get()
        appOpenAdManager =
            AppOpenAdManager(
                application = this,
                preferencesRepository = usageAndDiagnosticsRepository,
                scope = applicationScope,
            )

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager.loadAd(this)
    }

    override fun onTerminate() {
        unregisterActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        applicationScope.cancel()
        super.onTerminate()
    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        currentActivity?.let { activity ->
            appOpenAdManager.showAdIfAvailable(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
            appOpenAdManager.loadAd(activity.applicationContext)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }

    fun loadAppOpenAd(activity: Activity) {
        appOpenAdManager.loadAd(activity.applicationContext)
    }

    fun showAppOpenAdIfAvailable(activity: Activity) {
        appOpenAdManager.showAdIfAvailable(activity)
    }

    fun showAppOpenAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: AppOpenAdManager.OnShowAdCompleteListener,
    ) {
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }
}
