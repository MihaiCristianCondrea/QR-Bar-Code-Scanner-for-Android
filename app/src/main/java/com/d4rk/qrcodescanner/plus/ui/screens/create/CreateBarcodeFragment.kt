package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.loader.NativeAdPreloader
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementConfig
import com.d4rk.qrcodescanner.plus.ads.placement.NativeAdPlacementController
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateBarcodeBinding
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutParser
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateBarcodeAllActivity
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeAllActivity
import com.d4rk.qrcodescanner.plus.utils.extension.clipboardManager
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.zxing.BarcodeFormat
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateBarcodeFragment : Fragment() {

    private var _binding: FragmentCreateBarcodeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PreferenceListAdapter<PreferenceAction>
    private val nativeAds = mutableListOf<NativeAd>()
    private val adPlacementController = NativeAdPlacementController(
        NativeAdPlacementConfig(maxDensity = 0.3, minSpacing = 4, edgeBuffer = 1)
    )
    private var baseItems: List<PreferenceListItem<PreferenceAction>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MobileAds.initialize(requireContext())
        setupList()
    }

    private fun setupList() {
        adapter = PreferenceListAdapter(::handleActionClicked)
        binding.createList.layoutManager = LinearLayoutManager(requireContext())
        binding.createList.adapter = adapter
        FastScrollerBuilder(binding.createList).useMd2Style().build()
        baseItems = buildItems()
        adapter.submitList(baseItems)
        preloadAdsIfNeeded()
    }

    private fun buildItems(): List<PreferenceListItem<PreferenceAction>> {
        val entries =
            PreferenceLayoutParser.parse(requireContext(), R.xml.preferences_create_barcode)
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

        NativeAdPreloader.preload(
            context = requireContext(),
            adUnitId = getString(R.string.native_ad_support_unit_id),
            adRequest = AdRequest.Builder().build(),
            count = requestedAds,
            onFinished = { ads ->
                if (!isAdded) {
                    ads.forEach(NativeAd::destroy)
                    return@preload
                }
                if (ads.isEmpty()) {
                    Log.i(TAG, "No native ads returned for create fragment")
                    return@preload
                }
                nativeAds.forEach(NativeAd::destroy)
                nativeAds.clear()
                nativeAds.addAll(ads)
                adPlacementController.updateAds(nativeAds)
                val session = adPlacementController.beginSession()
                adapter.submitList(applyNativeAds(session))
            },
            onFailed = { error: LoadAdError ->
                Log.w(TAG, "Failed to preload native ads: ${error.message}")
            }
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
    ): List<PreferenceListItem<PreferenceAction>> {
        if (baseItems.isEmpty()) return baseItems
        val result = mutableListOf<PreferenceListItem<PreferenceAction>>()
        val sectionItems = mutableListOf<PreferenceListItem<PreferenceAction>>()

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

    private fun mapActionEntry(entry: PreferenceLayoutEntry.Action): PreferenceListItem.Action<PreferenceAction>? {
        val action = when (entry.key) {
            "clipboard" -> PreferenceAction.Clipboard
            "text" -> PreferenceAction.Text
            "url" -> PreferenceAction.Url
            "wifi" -> PreferenceAction.Wifi
            "location" -> PreferenceAction.Location
            "contact" -> PreferenceAction.Contact
            "more_qr_codes" -> PreferenceAction.MoreQrCodes
            "all_barcodes" -> PreferenceAction.AllBarcodes
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

    private fun handleActionClicked(action: PreferenceAction) {
        when (action) {
            PreferenceAction.Clipboard -> CreateBarcodeActivity.start(
                requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.OTHER, getClipboardContent()
            )

            PreferenceAction.Text -> CreateBarcodeActivity.start(
                requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.OTHER
            )

            PreferenceAction.Url -> CreateBarcodeActivity.start(
                requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.URL
            )

            PreferenceAction.Wifi -> CreateBarcodeActivity.start(
                requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.WIFI
            )

            PreferenceAction.Location -> CreateBarcodeActivity.start(
                requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.GEO
            )

            PreferenceAction.Contact -> CreateBarcodeActivity.start(
                requireActivity(), BarcodeFormat.QR_CODE, BarcodeSchema.VCARD
            )

            PreferenceAction.MoreQrCodes -> CreateQrCodeAllActivity.start(requireActivity())
            PreferenceAction.AllBarcodes -> CreateBarcodeAllActivity.start(requireActivity())
        }
    }

    private fun getClipboardContent(): String {
        val clip = requireActivity().clipboardManager?.primaryClip ?: return ""
        return when (clip.itemCount.orZero()) {
            0 -> ""
            else -> clip.getItemAt(0).text.toString()
        }
    }

    private enum class PreferenceAction {
        Clipboard, Text, Url, Wifi, Location, Contact, MoreQrCodes, AllBarcodes
    }

    override fun onDestroyView() {
        binding.createList.adapter = null
        nativeAds.forEach(NativeAd::destroy)
        nativeAds.clear()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "CreateBarcodeFragment"
    }
}
