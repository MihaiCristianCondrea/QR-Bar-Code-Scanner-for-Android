package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.loader.NativeAdPreloader
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementConfig
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateQrCodeAllBinding
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutParser
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateBarcodeActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
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
    private lateinit var adapter: PreferenceListAdapter<QrAction>
    private val nativeAds = mutableListOf<NativeAd>()
    private val adPlacementController = NativeAdPlacementController(
        NativeAdPlacementConfig(maxDensity = 0.3, minSpacing = 4, edgeBuffer = 1)
    )
    private var baseItems: List<PreferenceListItem<QrAction>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateQrCodeAllBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        MobileAds.initialize(this)
        setupList()
    }

    private fun setupList() {
        adapter = PreferenceListAdapter(::handleAction)
        binding.preferenceList.layoutManager = LinearLayoutManager(this)
        binding.preferenceList.adapter = adapter
        FastScrollerBuilder(binding.preferenceList).useMd2Style().build()
        baseItems = buildItems()
        adapter.submitList(baseItems)
        preloadAdsIfNeeded()
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

    private fun preloadAdsIfNeeded() {
        val requestedAds = estimateAdCount()
        if (requestedAds <= 0) {
            return
        }

        val adUnitId = getString(R.string.native_ad_support_unit_id)
        NativeAdPreloader.preload(
            context = this,
            adUnitId = adUnitId,
            adRequest = AdRequest.Builder().build(),
            count = requestedAds,
            onFinished = { ads ->
                if (ads.isEmpty()) {
                    return@preload
                }
                nativeAds.forEach(NativeAd::destroy)
                nativeAds.clear()
                nativeAds.addAll(ads)
                adPlacementController.updateAds(nativeAds)
                val session = adPlacementController.beginSession()
                adapter.submitList(applyNativeAds(session))
            },
        )
    }

    private fun estimateAdCount(): Int {
        if (baseItems.isEmpty()) return 0
        var count = 0
        var sectionSize = 0
        baseItems.forEach { item ->
            if (item is PreferenceListItem.Category) {
                if (sectionSize > 0) {
                    count += adPlacementController.expectedAdCount(sectionSize)
                    sectionSize = 0
                }
            } else {
                sectionSize++
            }
        }
        if (sectionSize > 0) {
            count += adPlacementController.expectedAdCount(sectionSize)
        }
        return count
    }

    private fun applyNativeAds(
        session: NativeAdPlacementController.PlacementSession
    ): List<PreferenceListItem<QrAction>> {
        if (baseItems.isEmpty()) return baseItems
        val result = mutableListOf<PreferenceListItem<QrAction>>()
        val sectionItems = mutableListOf<PreferenceListItem<QrAction>>()

        fun flushSection() {
            if (sectionItems.isEmpty()) return
            val decorated = session.decorate(sectionItems) { ad ->
                PreferenceListItem.NativeAd(nativeAd = ad)
            }
            result += decorated
            sectionItems.clear()
        }

        baseItems.forEach { item ->
            if (item is PreferenceListItem.Category) {
                flushSection()
                result += item
            } else {
                sectionItems += item
            }
        }

        flushSection()
        return result
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

    private fun handleAction(action: QrAction) {
        when (action) {
            QrAction.Text -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.OTHER
            )

            QrAction.Url -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.URL
            )

            QrAction.Wifi -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.WIFI
            )

            QrAction.Location -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.GEO
            )

            QrAction.Otp -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.OTP_AUTH
            )

            QrAction.ContactVcard -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.VCARD
            )

            QrAction.ContactMecard -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.MECARD
            )

            QrAction.Event -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.VEVENT
            )

            QrAction.Phone -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.PHONE
            )

            QrAction.Email -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.EMAIL
            )

            QrAction.Sms -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.SMS
            )

            QrAction.Mms -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.MMS
            )

            QrAction.Cryptocurrency -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.CRYPTOCURRENCY
            )

            QrAction.Bookmark -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.BOOKMARK
            )

            QrAction.App -> CreateBarcodeActivity.start(
                this,
                BarcodeFormat.QR_CODE,
                BarcodeSchema.APP
            )
        }
    }

    private enum class QrAction {
        Text, Url, Wifi, Location, Otp, ContactVcard, ContactMecard, Event, Phone, Email, Sms, Mms, Cryptocurrency, Bookmark, App
    }

    override fun onDestroy() {
        nativeAds.forEach(NativeAd::destroy)
        nativeAds.clear()
        super.onDestroy()
    }
}
