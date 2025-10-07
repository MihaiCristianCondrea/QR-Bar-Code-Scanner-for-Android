package com.d4rk.qrcodescanner.plus.di

import android.content.Context
import androidx.room.Room
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.create.ContactHelper
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.domain.history.BARCODE_DATABASE_MIGRATION_1_2
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabaseFactory
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.scan.ScannerCameraHelper
import com.d4rk.qrcodescanner.plus.utils.PermissionsHelper
import com.d4rk.qrcodescanner.plus.utils.RotationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBarcodeDatabaseFactory(
        @ApplicationContext context : Context
    ) : BarcodeDatabaseFactory {
        return Room.databaseBuilder(
            context ,
            BarcodeDatabaseFactory::class.java ,
            "db"
        ).addMigrations(BARCODE_DATABASE_MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideBarcodeDatabase(database : BarcodeDatabaseFactory) : BarcodeDatabase {
        return database.getBarcodeDatabase()
    }

    @Provides
    @Singleton
    fun provideBarcodeParser() : BarcodeParser = BarcodeParser

    @Provides
    @Singleton
    fun provideBarcodeImageScanner() : BarcodeImageScanner = BarcodeImageScanner

    @Provides
    @Singleton
    fun provideBarcodeImageGenerator() : BarcodeImageGenerator = BarcodeImageGenerator

    @Provides
    @Singleton
    fun provideBarcodeSaver() : BarcodeSaver = BarcodeSaver

    @Provides
    @Singleton
    fun provideBarcodeImageSaver() : BarcodeImageSaver = BarcodeImageSaver

    @Provides
    @Singleton
    fun provideWifiConnector() : WifiConnector = WifiConnector

    @Provides
    @Singleton
    fun provideOtpGenerator() : OTPGenerator = OTPGenerator

    @Provides
    @Singleton
    fun provideContactHelper() : ContactHelper = ContactHelper

    @Provides
    @Singleton
    fun providePermissionsHelper() : PermissionsHelper = PermissionsHelper

    @Provides
    @Singleton
    fun provideRotationHelper() : RotationHelper = RotationHelper

    @Provides
    @Singleton
    fun provideScannerCameraHelper() : ScannerCameraHelper = ScannerCameraHelper
}
