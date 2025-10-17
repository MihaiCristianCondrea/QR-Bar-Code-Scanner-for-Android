package com.d4rk.qrcodescanner.plus.di

import androidx.room.Room
import com.d4rk.qrcodescanner.plus.data.engagement.SharedPreferencesAppEngagementRepository
import com.d4rk.qrcodescanner.plus.data.settings.SharedPreferencesMainPreferencesRepository
import com.d4rk.qrcodescanner.plus.data.settings.SharedPreferencesUsageAndDiagnosticsRepository
import com.d4rk.qrcodescanner.plus.data.support.GoogleSupportRepository
import com.d4rk.qrcodescanner.plus.data.support.SupportRepository
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeDetailsRepository
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.create.ContactHelper
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.domain.engagement.AppEngagementRepository
import com.d4rk.qrcodescanner.plus.domain.history.BARCODE_DATABASE_MIGRATION_1_2
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabaseFactory
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeHistoryRepository
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.support.InitBillingClientUseCase
import com.d4rk.qrcodescanner.plus.domain.support.InitMobileAdsUseCase
import com.d4rk.qrcodescanner.plus.domain.support.InitiatePurchaseUseCase
import com.d4rk.qrcodescanner.plus.domain.support.QueryProductDetailsUseCase
import com.d4rk.qrcodescanner.plus.domain.support.RefreshPurchasesUseCase
import com.d4rk.qrcodescanner.plus.domain.support.SetPurchaseStatusListenerUseCase
import com.d4rk.qrcodescanner.plus.utils.helpers.PermissionsHelper
import com.d4rk.qrcodescanner.plus.utils.helpers.RotationHelper
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            BarcodeDatabaseFactory::class.java,
            "db"
        ).addMigrations(BARCODE_DATABASE_MIGRATION_1_2).build()
    }

    single<BarcodeDatabase> { get<BarcodeDatabaseFactory>().getBarcodeDatabase() }

    single { BarcodeHistoryRepository(get()) }

    single { Settings(androidContext()) }

    single<MainPreferencesRepository> { SharedPreferencesMainPreferencesRepository(androidContext()) }
    single<UsageAndDiagnosticsPreferencesRepository> {
        SharedPreferencesUsageAndDiagnosticsRepository(androidContext())
    }
    single<AppEngagementRepository> { SharedPreferencesAppEngagementRepository(androidContext()) }

    single { BarcodeParser }
    single { BarcodeImageScanner }
    single { BarcodeImageGenerator }
    single { BarcodeSaver }
    single { BarcodeImageSaver }
    single { BarcodeDetailsRepository(get(), Dispatchers.IO) }
    single { WifiConnector }
    single { OTPGenerator }
    single { ContactHelper }
    single { PermissionsHelper }
    single { RotationHelper }
    single<SupportRepository> { GoogleSupportRepository(androidContext()) }
    single { InitBillingClientUseCase(get()) }
    single { QueryProductDetailsUseCase(get()) }
    single { InitiatePurchaseUseCase(get()) }
    single { InitMobileAdsUseCase(get()) }
    single { RefreshPurchasesUseCase(get()) }
    single { SetPurchaseStatusListenerUseCase(get()) }
}
