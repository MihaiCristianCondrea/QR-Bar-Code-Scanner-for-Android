package com.d4rk.qrcodescanner.plus.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.QrCodeScanner
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.create.ContactHelper
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.scan.ScannerCameraHelper
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.utils.PermissionsHelper
import com.d4rk.qrcodescanner.plus.utils.RotationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AppEntryPoint {
    fun settings() : Settings
    fun barcodeParser() : BarcodeParser
    fun barcodeImageScanner() : BarcodeImageScanner
    fun barcodeImageGenerator() : BarcodeImageGenerator
    fun barcodeSaver() : BarcodeSaver
    fun barcodeImageSaver() : BarcodeImageSaver
    fun wifiConnector() : WifiConnector
    fun otpGenerator() : OTPGenerator
    fun barcodeDatabase() : BarcodeDatabase
    fun contactHelper() : ContactHelper
    fun permissionsHelper() : PermissionsHelper
    fun rotationHelper() : RotationHelper
    fun scannerCameraHelper() : ScannerCameraHelper
}

private fun Context.appEntryPoint() : AppEntryPoint {
    return EntryPointAccessors.fromApplication(
        applicationContext ,
        AppEntryPoint::class.java
    )
}

private val applicationEntryPoint : AppEntryPoint
    get() = QrCodeScanner.instance.appEntryPoint()

val QrCodeScanner.settings : Settings
    get() = appEntryPoint().settings()

val barcodeParser : BarcodeParser
    get() = applicationEntryPoint.barcodeParser()

val barcodeImageScanner : BarcodeImageScanner
    get() = applicationEntryPoint.barcodeImageScanner()

val barcodeImageGenerator : BarcodeImageGenerator
    get() = applicationEntryPoint.barcodeImageGenerator()

val barcodeSaver : BarcodeSaver
    get() = applicationEntryPoint.barcodeSaver()

val barcodeImageSaver : BarcodeImageSaver
    get() = applicationEntryPoint.barcodeImageSaver()

val wifiConnector : WifiConnector
    get() = applicationEntryPoint.wifiConnector()

val otpGenerator : OTPGenerator
    get() = applicationEntryPoint.otpGenerator()

val AppCompatActivity.barcodeDatabase : BarcodeDatabase
    get() = appEntryPoint().barcodeDatabase()

val AppCompatActivity.settings : Settings
    get() = appEntryPoint().settings()

val contactHelper : ContactHelper
    get() = applicationEntryPoint.contactHelper()

val permissionsHelper : PermissionsHelper
    get() = applicationEntryPoint.permissionsHelper()

val rotationHelper : RotationHelper
    get() = applicationEntryPoint.rotationHelper()

val scannerCameraHelper : ScannerCameraHelper
    get() = applicationEntryPoint.scannerCameraHelper()

val Fragment.barcodeDatabase : BarcodeDatabase
    get() = requireContext().appEntryPoint().barcodeDatabase()

val Fragment.settings : Settings
    get() = requireContext().appEntryPoint().settings()
