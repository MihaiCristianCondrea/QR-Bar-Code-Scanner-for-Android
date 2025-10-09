package com.d4rk.qrcodescanner.plus.di

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.create.ContactHelper
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.domain.main.MainPreferencesRepository
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.scan.ScannerCameraHelper
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.utils.PermissionsHelper
import com.d4rk.qrcodescanner.plus.utils.RotationHelper
import org.koin.core.context.GlobalContext

private val koin get() = GlobalContext.get()

val barcodeParser: BarcodeParser
    get() = koin.get()

val barcodeImageScanner: BarcodeImageScanner
    get() = koin.get()

val barcodeImageGenerator: BarcodeImageGenerator
    get() = koin.get()

val barcodeSaver: BarcodeSaver
    get() = koin.get()

val barcodeImageSaver: BarcodeImageSaver
    get() = koin.get()

val wifiConnector: WifiConnector
    get() = koin.get()

val otpGenerator: OTPGenerator
    get() = koin.get()

val contactHelper: ContactHelper
    get() = koin.get()

val permissionsHelper: PermissionsHelper
    get() = koin.get()

val rotationHelper: RotationHelper
    get() = koin.get()

val scannerCameraHelper: ScannerCameraHelper
    get() = koin.get()

val AppCompatActivity.barcodeDatabase: BarcodeDatabase
    get() = koin.get()

val AppCompatActivity.settings: Settings
    get() = koin.get()

val AppCompatActivity.mainPreferencesRepository: MainPreferencesRepository
    get() = koin.get()

val Fragment.barcodeDatabase: BarcodeDatabase
    get() = koin.get()

val Fragment.settings: Settings
    get() = koin.get()

