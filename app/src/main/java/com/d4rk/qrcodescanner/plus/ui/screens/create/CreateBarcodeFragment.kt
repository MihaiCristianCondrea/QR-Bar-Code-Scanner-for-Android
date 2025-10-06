package com.d4rk.qrcodescanner.plus.ui.screens.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateBarcodeBinding
import com.d4rk.qrcodescanner.plus.extension.clipboardManager
import com.d4rk.qrcodescanner.plus.extension.orZero
import com.d4rk.qrcodescanner.plus.model.schema.BarcodeSchema
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.screens.create.barcode.CreateBarcodeAllActivity
import com.d4rk.qrcodescanner.plus.ui.screens.create.qr.CreateQrCodeAllActivity
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
        return listOf(
            PreferenceListItem.Category(R.string.qr_code), PreferenceListItem.Action(
                action = PreferenceAction.Clipboard,
                titleRes = R.string.content_from_clipboard,
                iconRes = R.drawable.ic_copy
            ), PreferenceListItem.Action(
                action = PreferenceAction.Text,
                titleRes = R.string.text,
                iconRes = R.drawable.ic_text
            ), PreferenceListItem.Action(
                action = PreferenceAction.Url, titleRes = R.string.url, iconRes = R.drawable.ic_link
            ), PreferenceListItem.Action(
                action = PreferenceAction.Wifi,
                titleRes = R.string.wifi,
                iconRes = R.drawable.ic_wifi
            ), PreferenceListItem.Action(
                action = PreferenceAction.Location,
                titleRes = R.string.location,
                iconRes = R.drawable.ic_location
            ), PreferenceListItem.Action(
                action = PreferenceAction.Contact,
                titleRes = R.string.contact_v_card,
                iconRes = R.drawable.ic_contact_white
            ), PreferenceListItem.Action(
                action = PreferenceAction.MoreQrCodes,
                titleRes = R.string.more_qr_codes,
                iconRes = R.drawable.ic_qr_code_white,
                widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ), PreferenceListItem.Category(R.string.barcode), PreferenceListItem.Action(
                action = PreferenceAction.AllBarcodes,
                titleRes = R.string.all_barcodes_codes,
                iconRes = R.drawable.ic_barcode,
                widgetLayoutRes = R.layout.item_preference_widget_open_internal
            )
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
