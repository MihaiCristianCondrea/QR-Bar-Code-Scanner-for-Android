package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateBarcodeBinding
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutParser
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.components.preferences.withMiddleNativeAd
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateBarcodeAllActivity
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeAllActivity
import com.d4rk.qrcodescanner.plus.utils.extension.clipboardManager
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.google.android.gms.ads.MobileAds
import com.google.zxing.BarcodeFormat
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateBarcodeFragment : Fragment() {

    private var _binding: FragmentCreateBarcodeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PreferenceListAdapter<PreferenceAction>

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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.createList.adapter = null
        _binding = null
    }

    private fun setupList() {
        adapter = PreferenceListAdapter(::handleActionClicked)
        binding.createList.layoutManager = LinearLayoutManager(requireContext())
        binding.createList.adapter = adapter
        FastScrollerBuilder(binding.createList).useMd2Style().build()
        adapter.submitList(buildItems())
    }

    private fun buildItems(): List<PreferenceListItem<PreferenceAction>> {
        val entries =
            PreferenceLayoutParser.parse(requireContext(), R.xml.preferences_create_barcode)
        return entries.mapNotNull { entry ->
            when (entry) {
                is PreferenceLayoutEntry.Category -> PreferenceListItem.Category(entry.titleRes)
                is PreferenceLayoutEntry.Action -> mapActionEntry(entry)
            }
        }.withMiddleNativeAd()
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
}
