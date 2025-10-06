package com.d4rk.qrcodescanner.plus.ui.screens.create.qr
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateQrCodeAllBinding
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateBarcodeActivity
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.zxing.BarcodeFormat
import me.zhanghai.android.fastscroll.FastScrollerBuilder
class CreateQrCodeAllActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CreateQrCodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }
    private lateinit var binding: ActivityCreateQrCodeAllBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateQrCodeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        handleButtonsClicked()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }
    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }
    private fun handleButtonsClicked() {
        binding.buttonText.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.OTHER)
        }
        binding.buttonUrl.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.URL)
        }
        binding.buttonWifi.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.WIFI)
        }
        binding.buttonLocation.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.GEO)
        }
        binding.buttonOtp.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.OTP_AUTH)
        }
        binding.buttonContactVcard.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.VCARD)
        }
        binding.buttonContactMecard.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.MECARD)
        }
        binding.buttonEvent.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.VEVENT)
        }
        binding.buttonPhone.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.PHONE)
        }
        binding.buttonEmail.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.EMAIL)
        }
        binding.buttonSms.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.SMS)
        }
        binding.buttonMms.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.MMS)
        }
        binding.buttonCryptocurrency.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.CRYPTOCURRENCY)
        }
        binding.buttonBookmark.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.BOOKMARK)
        }
        binding.buttonApp.setOnClickListener {
            CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.APP)
        }
    }
}