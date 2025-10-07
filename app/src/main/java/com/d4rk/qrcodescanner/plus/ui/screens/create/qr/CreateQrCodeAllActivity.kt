package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateQrCodeAllBinding
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutParser
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateBarcodeActivity
import com.google.zxing.BarcodeFormat
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateQrCodeAllActivity : BaseActivity() {
    companion object {
        fun start(context : Context) {
            val intent = Intent(context , CreateQrCodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding : ActivityCreateQrCodeAllBinding
    private lateinit var adapter : PreferenceListAdapter<QrAction>

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateQrCodeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        setupList()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true , applyBottom = true)
    }

    private fun setupList() {
        adapter = PreferenceListAdapter(::handleAction)
        binding.preferenceList.layoutManager = LinearLayoutManager(this)
        binding.preferenceList.adapter = adapter
        FastScrollerBuilder(binding.preferenceList).useMd2Style().build()
        adapter.submitList(buildItems())
    }

    private fun buildItems(): List<PreferenceListItem<QrAction>> {
        val entries = PreferenceLayoutParser.parse(this, R.xml.preferences_create_qr_all)
        return entries.mapNotNull { entry ->
            when (entry) {
                is PreferenceLayoutEntry.Category -> PreferenceListItem.Category(entry.titleRes)
                is PreferenceLayoutEntry.Action -> mapActionEntry(entry)
            }
        }
    }

    private fun mapActionEntry(entry: PreferenceLayoutEntry.Action): PreferenceListItem.Action<QrAction>? {
        val action = when (entry.key) {
            "text" -> QrAction.Text
            "url" -> QrAction.Url
            "wifi" -> QrAction.Wifi
            "location" -> QrAction.Location
            "otp" -> QrAction.Otp
            "contact_vcard" -> QrAction.ContactVcard
            "contact_mecard" -> QrAction.ContactMecard
            "event" -> QrAction.Event
            "phone" -> QrAction.Phone
            "email" -> QrAction.Email
            "sms" -> QrAction.Sms
            "mms" -> QrAction.Mms
            "cryptocurrency" -> QrAction.Cryptocurrency
            "bookmark" -> QrAction.Bookmark
            "app" -> QrAction.App
            else -> null
        } ?: return null

        return PreferenceListItem.Action(
            action = action,
            titleRes = entry.titleRes,
            summaryRes = entry.summaryRes,
            iconRes = entry.iconRes,
            widgetLayoutRes = entry.widgetLayoutRes
        )
    }

    private fun handleAction(action : QrAction) {
        when (action) {
            QrAction.Text -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.OTHER)
            QrAction.Url -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.URL)
            QrAction.Wifi -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.WIFI)
            QrAction.Location -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.GEO)
            QrAction.Otp -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.OTP_AUTH)
            QrAction.ContactVcard -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.VCARD)
            QrAction.ContactMecard -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.MECARD)
            QrAction.Event -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.VEVENT)
            QrAction.Phone -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.PHONE)
            QrAction.Email -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.EMAIL)
            QrAction.Sms -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.SMS)
            QrAction.Mms -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.MMS)
            QrAction.Cryptocurrency -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.CRYPTOCURRENCY)
            QrAction.Bookmark -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.BOOKMARK)
            QrAction.App -> CreateBarcodeActivity.start(this , BarcodeFormat.QR_CODE , BarcodeSchema.APP)
        }
    }

    private enum class QrAction {
        Text , Url , Wifi , Location , Otp , ContactVcard , ContactMecard , Event , Phone , Email , Sms , Mms , Cryptocurrency , Bookmark , App
    }
}
