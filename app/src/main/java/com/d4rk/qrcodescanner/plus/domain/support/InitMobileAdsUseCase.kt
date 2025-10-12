package com.d4rk.qrcodescanner.plus.domain.support

import com.d4rk.qrcodescanner.plus.data.support.SupportRepository
import com.google.android.gms.ads.AdRequest

class InitMobileAdsUseCase(private val repository: SupportRepository) {

    operator fun invoke(): AdRequest {
        return repository.initMobileAds()
    }
}
