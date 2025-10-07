package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateBarcodeAllBinding
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutParser
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
        val entries = PreferenceLayoutParser.parse(this , R.xml.preferences_create_barcode_all)
        return entries.mapNotNull { entry ->
            when (entry) {
                is PreferenceLayoutEntry.Category -> PreferenceListItem.Category(entry.titleRes)
                is PreferenceLayoutEntry.Action -> mapActionEntry(entry)
            }
        }
    }

    private fun mapActionEntry(entry : PreferenceLayoutEntry.Action) : PreferenceListItem.Action<BarcodeAction>? {
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
