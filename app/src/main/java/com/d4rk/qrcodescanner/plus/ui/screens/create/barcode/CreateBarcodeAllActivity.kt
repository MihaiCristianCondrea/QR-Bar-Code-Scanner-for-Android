package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.loader.NativeAdPreloader
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementConfig
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateBarcodeAllBinding
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

class CreateBarcodeAllActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CreateBarcodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityCreateBarcodeAllBinding
    private lateinit var adapter: PreferenceListAdapter<BarcodeAction>
    private val nativeAds = mutableListOf<NativeAd>()
    private val adPlacementController = NativeAdPlacementController(
        NativeAdPlacementConfig(maxDensity = 0.3, minSpacing = 4, edgeBuffer = 1)
    )
    private var baseItems: List<PreferenceListItem<BarcodeAction>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBarcodeAllBinding.inflate(layoutInflater)
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

    private fun buildItems(): List<PreferenceListItem<BarcodeAction>> {
        val entries = PreferenceLayoutParser.parse(this, R.xml.preferences_create_barcode_all)
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

        val request = AdRequest.Builder().build()
        val adUnitId = getString(R.string.native_ad_support_unit_id)
        NativeAdPreloader.preload(
            context = this,
            adUnitId = adUnitId,
            adRequest = request,
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
    ): List<PreferenceListItem<BarcodeAction>> {
        if (baseItems.isEmpty()) return baseItems
        val result = mutableListOf<PreferenceListItem<BarcodeAction>>()
        val sectionItems = mutableListOf<PreferenceListItem<BarcodeAction>>()

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

    private fun mapActionEntry(entry: PreferenceLayoutEntry.Action): PreferenceListItem.Action<BarcodeAction>? {
        val action = when (entry.key) {
            "data_matrix" -> BarcodeAction.DataMatrix
            "aztec" -> BarcodeAction.Aztec
            "pdf417" -> BarcodeAction.Pdf417
            "ean_13" -> BarcodeAction.Ean13
            "ean_8" -> BarcodeAction.Ean8
            "upc_e" -> BarcodeAction.UpcE
            "upc_a" -> BarcodeAction.UpcA
            "code_128" -> BarcodeAction.Code128
            "code_93" -> BarcodeAction.Code93
            "code_39" -> BarcodeAction.Code39
            "codabar" -> BarcodeAction.Codabar
            "itf" -> BarcodeAction.Itf
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

    private fun handleAction(action: BarcodeAction) {
        when (action) {
            BarcodeAction.DataMatrix -> CreateBarcodeActivity.start(this, BarcodeFormat.DATA_MATRIX)
            BarcodeAction.Aztec -> CreateBarcodeActivity.start(this, BarcodeFormat.AZTEC)
            BarcodeAction.Pdf417 -> CreateBarcodeActivity.start(this, BarcodeFormat.PDF_417)
            BarcodeAction.Codabar -> CreateBarcodeActivity.start(this, BarcodeFormat.CODABAR)
            BarcodeAction.Code39 -> CreateBarcodeActivity.start(this, BarcodeFormat.CODE_39)
            BarcodeAction.Code93 -> CreateBarcodeActivity.start(this, BarcodeFormat.CODE_93)
            BarcodeAction.Code128 -> CreateBarcodeActivity.start(this, BarcodeFormat.CODE_128)
            BarcodeAction.Ean8 -> CreateBarcodeActivity.start(this, BarcodeFormat.EAN_8)
            BarcodeAction.Ean13 -> CreateBarcodeActivity.start(this, BarcodeFormat.EAN_13)
            BarcodeAction.Itf -> CreateBarcodeActivity.start(this, BarcodeFormat.ITF)
            BarcodeAction.UpcA -> CreateBarcodeActivity.start(this, BarcodeFormat.UPC_A)
            BarcodeAction.UpcE -> CreateBarcodeActivity.start(this, BarcodeFormat.UPC_E)
        }
    }

    private enum class BarcodeAction {
        DataMatrix, Aztec, Pdf417, Codabar, Code39, Code93, Code128, Ean8, Ean13, Itf, UpcA, UpcE
    }

    override fun onDestroy() {
        nativeAds.forEach(NativeAd::destroy)
        nativeAds.clear()
        super.onDestroy()
    }
}
