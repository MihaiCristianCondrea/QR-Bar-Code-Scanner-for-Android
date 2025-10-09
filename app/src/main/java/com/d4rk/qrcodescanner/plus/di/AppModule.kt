package com.d4rk.qrcodescanner.plus.di

import androidx.room.Room
import com.d4rk.qrcodescanner.plus.data.settings.SharedPreferencesMainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.create.ContactHelper
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.domain.history.BARCODE_DATABASE_MIGRATION_1_2
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabaseFactory
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.scan.ScannerCameraHelper
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.utils.PermissionsHelper
import com.d4rk.qrcodescanner.plus.utils.RotationHelper
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

    single { Settings(androidContext()) }

    single<MainPreferencesRepository> { SharedPreferencesMainPreferencesRepository(androidContext()) }

    single { BarcodeParser }
    single { BarcodeImageScanner }
    single { BarcodeImageGenerator }
    single { BarcodeSaver }
    single { BarcodeImageSaver }
    single { WifiConnector }
    single { OTPGenerator }
    single { ContactHelper }
    single { PermissionsHelper }
    single { RotationHelper }
    single { ScannerCameraHelper }
}
