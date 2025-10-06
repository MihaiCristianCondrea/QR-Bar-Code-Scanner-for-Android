package com.d4rk.qrcodescanner.plus.ui.screens.history.export
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityExportHistoryBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeSaver
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.extension.showError
import com.d4rk.qrcodescanner.plus.extension.textString
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ExportHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityExportHistoryBinding
    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 101
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        fun start(context: Context) {
            val intent = Intent(context, ExportHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        initExportTypeSpinner()
        initFileNameEditText()
        initExportButton()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsHelper.areAllPermissionsGranted(grantResults)) {
            exportHistory()
        }
    }
    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }
    private fun initExportTypeSpinner() {
        binding.spinnerExportAs.adapter = ArrayAdapter.createFromResource(
            this, R.array.activity_export_history_types, R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }
    private fun initFileNameEditText() {
        binding.editTextFileName.addTextChangedListener {
            binding.buttonExport.isEnabled = binding.editTextFileName.isNotBlank()
        }
    }
    private fun initExportButton() {
        binding.buttonExport.setOnClickListener {
            requestPermissions()
        }
    }
    private fun requestPermissions() {
        permissionsHelper.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE)
    }
    private fun exportHistory() {
        val fileName = binding.editTextFileName.textString
        val saveFunc = when (binding.spinnerExportAs.selectedItemPosition) {
            0 -> barcodeSaver::saveBarcodeHistoryAsCsv
            1 -> barcodeSaver::saveBarcodeHistoryAsJson
            else -> return
        }
        showLoading(true)
        lifecycleScope.launch {
            try {
                val barcodes = withContext(Dispatchers.IO) { barcodeDatabase.getAllForExport().first() }
                withContext(Dispatchers.IO) { saveFunc(this@ExportHistoryActivity, fileName, barcodes) }
                showHistoryExported()
            } catch (error: Exception) {
                showLoading(false)
                showError(error)
            }
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLoading.isVisible = isLoading
        binding.scrollView.isVisible = isLoading.not()
    }
    private fun showHistoryExported() {
        Snackbar.make(binding.root, R.string.snack_saved_to_downloads, Snackbar.LENGTH_LONG).show()
        finish()
    }
}