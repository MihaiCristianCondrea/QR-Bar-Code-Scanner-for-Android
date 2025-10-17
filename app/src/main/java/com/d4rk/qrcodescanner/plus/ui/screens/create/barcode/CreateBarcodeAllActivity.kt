package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityCreateBarcodeAllBinding
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceLayoutEntry
import com.d4rk.qrcodescanner.plus.ui.components.preferences.PreferenceListItem
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateAllPreferenceListController
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateBarcodeActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.gms.ads.MobileAds
import com.google.zxing.BarcodeFormat

class CreateBarcodeAllActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CreateBarcodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityCreateBarcodeAllBinding
    private lateinit var preferenceListController: CreateAllPreferenceListController<BarcodeAction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBarcodeAllBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        MobileAds.initialize(this)
        preferenceListController = CreateAllPreferenceListController(
            context = this,
            recyclerView = binding.preferenceList,
            preferencesXmlRes = R.xml.preferences_create_barcode_all,
            mapActionEntry = ::mapActionEntry,
            onActionSelected = ::handleAction,
        )
        preferenceListController.initialize()
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
        preferenceListController.destroy()
        super.onDestroy()
    }
}
