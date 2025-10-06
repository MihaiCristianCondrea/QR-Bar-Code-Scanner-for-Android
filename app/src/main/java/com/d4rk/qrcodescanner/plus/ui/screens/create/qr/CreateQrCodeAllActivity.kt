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

    private fun buildItems() : List<PreferenceListItem<QrAction>> {
        return listOf(
            PreferenceListItem.Action(
                action = QrAction.Text , titleRes = R.string.text , iconRes = R.drawable.ic_text , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Url , titleRes = R.string.url , iconRes = R.drawable.ic_link , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Wifi , titleRes = R.string.wifi , iconRes = R.drawable.ic_wifi , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Location , titleRes = R.string.location , iconRes = R.drawable.ic_location , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Otp , titleRes = R.string.otp , iconRes = R.drawable.ic_otp , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.ContactVcard , titleRes = R.string.contact_v_card , iconRes = R.drawable.ic_contact_white , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.ContactMecard , titleRes = R.string.contact_me_card , iconRes = R.drawable.ic_contact_white , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Event , titleRes = R.string.event , iconRes = R.drawable.ic_calendar , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Phone , titleRes = R.string.phone , iconRes = R.drawable.ic_phone , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Email , titleRes = R.string.email , iconRes = R.drawable.ic_email , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Sms , titleRes = R.string.sms , iconRes = R.drawable.ic_sms , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Mms , titleRes = R.string.mms , iconRes = R.drawable.ic_mms , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Cryptocurrency , titleRes = R.string.bitcoin , iconRes = R.drawable.ic_bitcoin , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.Bookmark , titleRes = R.string.bookmark , iconRes = R.drawable.ic_bookmark , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = QrAction.App , titleRes = R.string.app , iconRes = R.drawable.ic_app , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            )
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
