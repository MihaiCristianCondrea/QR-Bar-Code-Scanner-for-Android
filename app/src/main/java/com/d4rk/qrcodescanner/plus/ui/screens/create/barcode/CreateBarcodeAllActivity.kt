package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateBarcodeAllBinding
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListAdapter
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateBarcodeActivity
import com.google.zxing.BarcodeFormat
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class CreateBarcodeAllActivity : BaseActivity() {
    companion object {
        fun start(context : Context) {
            val intent = Intent(context , CreateBarcodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding : ActivityCreateBarcodeAllBinding
    private lateinit var adapter : PreferenceListAdapter<BarcodeAction>

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBarcodeAllBinding.inflate(layoutInflater)
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

    private fun buildItems() : List<PreferenceListItem<BarcodeAction>> {
        return listOf(
            PreferenceListItem.Category(R.string.barcodes_2d) , PreferenceListItem.Action(
                action = BarcodeAction.DataMatrix , titleRes = R.string.data_matrix , iconRes = R.drawable.ic_data_matrix , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Aztec , titleRes = R.string.aztec , iconRes = R.drawable.ic_aztec , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Pdf417 , titleRes = R.string.pdf_417 , iconRes = R.drawable.ic_pdf417 , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Category(R.string.barcodes_1d) , PreferenceListItem.Action(
                action = BarcodeAction.Ean13 , titleRes = R.string.ean_13 , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Ean8 , titleRes = R.string.ean_8 , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.UpcE , titleRes = R.string.upc_e , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.UpcA , titleRes = R.string.upc_a , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Code128 , titleRes = R.string.code_128 , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Code93 , titleRes = R.string.code_93 , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Code39 , titleRes = R.string.code_39 , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Codabar , titleRes = R.string.codabar , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            ) , PreferenceListItem.Action(
                action = BarcodeAction.Itf , titleRes = R.string.itf , iconRes = R.drawable.ic_barcode , widgetLayoutRes = R.layout.item_preference_widget_open_internal
            )
        )
    }

    private fun handleAction(action : BarcodeAction) {
        when (action) {
            BarcodeAction.DataMatrix -> CreateBarcodeActivity.start(this , BarcodeFormat.DATA_MATRIX)
            BarcodeAction.Aztec -> CreateBarcodeActivity.start(this , BarcodeFormat.AZTEC)
            BarcodeAction.Pdf417 -> CreateBarcodeActivity.start(this , BarcodeFormat.PDF_417)
            BarcodeAction.Codabar -> CreateBarcodeActivity.start(this , BarcodeFormat.CODABAR)
            BarcodeAction.Code39 -> CreateBarcodeActivity.start(this , BarcodeFormat.CODE_39)
            BarcodeAction.Code93 -> CreateBarcodeActivity.start(this , BarcodeFormat.CODE_93)
            BarcodeAction.Code128 -> CreateBarcodeActivity.start(this , BarcodeFormat.CODE_128)
            BarcodeAction.Ean8 -> CreateBarcodeActivity.start(this , BarcodeFormat.EAN_8)
            BarcodeAction.Ean13 -> CreateBarcodeActivity.start(this , BarcodeFormat.EAN_13)
            BarcodeAction.Itf -> CreateBarcodeActivity.start(this , BarcodeFormat.ITF)
            BarcodeAction.UpcA -> CreateBarcodeActivity.start(this , BarcodeFormat.UPC_A)
            BarcodeAction.UpcE -> CreateBarcodeActivity.start(this , BarcodeFormat.UPC_E)
        }
    }

    private enum class BarcodeAction {
        DataMatrix , Aztec , Pdf417 , Codabar , Code39 , Code93 , Code128 , Ean8 , Ean13 , Itf , UpcA , UpcE
    }
}
