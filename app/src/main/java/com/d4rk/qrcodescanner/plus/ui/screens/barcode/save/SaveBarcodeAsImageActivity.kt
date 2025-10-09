package com.d4rk.qrcodescanner.plus.ui.screens.barcode.save

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivitySaveBarcodeAsImageBinding
import com.d4rk.qrcodescanner.plus.di.barcodeImageGenerator
import com.d4rk.qrcodescanner.plus.di.barcodeImageSaver
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class SaveBarcodeAsImageActivity : BaseActivity() {
    private lateinit var binding : ActivitySaveBarcodeAsImageBinding

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 101
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val BARCODE_KEY = "BARCODE_KEY"

        fun start(context : Context , barcode : Barcode) {
            val intent = Intent(context , SaveBarcodeAsImageActivity::class.java).apply {
                putExtra(BARCODE_KEY , barcode)
            }
            context.startActivity(intent)
        }
    }

    @Suppress("DEPRECATION")
    private val barcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBarcodeAsImageBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        initFormatSpinner()
        initSaveButton()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    override fun onRequestPermissionsResult(requestCode : Int , permissions : Array<out String> , grantResults : IntArray) {
        super.onRequestPermissionsResult(requestCode , permissions , grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE && permissionsHelper.areAllPermissionsGranted(grantResults)) {
            saveBarcode()
        }
    }

    private fun initFormatSpinner() {
        binding.spinnerSaveAs.adapter = ArrayAdapter.createFromResource(
            this , R.array.activity_save_barcode_as_image_formats , R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }

    private fun initSaveButton() {
        binding.buttonSave.setOnClickListener {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        permissionsHelper.requestPermissions(this , PERMISSIONS , REQUEST_PERMISSIONS_CODE)
    }

    private fun saveBarcode() {
        showLoading(true)
        lifecycleScope.launch {
            runCatching {
                when (binding.spinnerSaveAs.selectedItemPosition) {
                    0 -> {
                        barcodeImageGenerator.generateBitmap(barcode, 640, 640, 2)?.let {
                            barcodeImageSaver.savePngImageToPublicDirectory(this@SaveBarcodeAsImageActivity, it, barcode)
                        }
                    }
                    1 -> {
                        val svg = barcodeImageGenerator.generateSvg(barcode , 640 , 640 , 2)
                        barcodeImageSaver.saveSvgImageToPublicDirectory(this@SaveBarcodeAsImageActivity , svg , barcode)
                    }
                    else -> Unit
                }
            }.onSuccess {
                showBarcodeSaved()
            }.onFailure { e ->
                showLoading(false)
                showError(e)
            }.let { showLoading(false) }
        }
    }

    private fun showLoading(isLoading : Boolean) {
        binding.progressBarLoading.isVisible = isLoading
        binding.scrollView.isVisible = ! isLoading
    }

    private fun showBarcodeSaved() {
        Snackbar.make(binding.root , R.string.snack_saved_to_gallery , Snackbar.LENGTH_LONG).show()
        finish()
    }
}