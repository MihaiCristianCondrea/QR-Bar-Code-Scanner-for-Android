package com.d4rk.qrcodescanner.plus.di
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.QrCodeScanner
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageSaver
import com.d4rk.qrcodescanner.plus.domain.barcode.WifiConnector
import com.d4rk.qrcodescanner.plus.domain.create.ContactHelper
import com.d4rk.qrcodescanner.plus.domain.create.OTPGenerator
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabaseFactory
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeSaver
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.scan.ScannerCameraHelper
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.utils.PermissionsHelper
import com.d4rk.qrcodescanner.plus.utils.RotationHelper
val QrCodeScanner.settings get() = Settings.getInstance(applicationContext)
val barcodeParser get() = BarcodeParser
val barcodeImageScanner get() = BarcodeImageScanner
val barcodeImageGenerator get() = BarcodeImageGenerator
val barcodeSaver get() = BarcodeSaver
val barcodeImageSaver get() = BarcodeImageSaver
val wifiConnector get() = WifiConnector
val otpGenerator get() = OTPGenerator
val AppCompatActivity.barcodeDatabase get() = BarcodeDatabaseFactory.getInstance(this) // FIXME: Unresolved reference 'getInstance'.
val AppCompatActivity.settings get() = Settings.getInstance(this)
val contactHelper get() = ContactHelper
val permissionsHelper get() = PermissionsHelper
val rotationHelper get() = RotationHelper
val scannerCameraHelper get() = ScannerCameraHelper
val Fragment.barcodeDatabase get() = BarcodeDatabaseFactory.getInstance(requireContext()) // FIXME: Unresolved reference 'getInstance'.
val Fragment.settings get() = Settings.getInstance(requireContext())