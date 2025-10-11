package com.d4rk.qrcodescanner.plus.ui.screens.history.export

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityExportHistoryBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeSaver
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.extension.textString
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ExportHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityExportHistoryBinding
    private val viewModel: ExportHistoryViewModel by viewModels {
        ExportHistoryViewModelFactory(barcodeDatabase, barcodeSaver)
    }
    private var isExporting: Boolean = false

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
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        initExportTypeSpinner()
        initFileNameEditText()
        initExportButton()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
        observeUiState()
        updateExportButtonState()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsHelper.areAllPermissionsGranted(grantResults)) {
            exportHistory()
        }
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
            updateExportButtonState()
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
        val exportType =
            ExportType.fromSpinnerIndex(binding.spinnerExportAs.selectedItemPosition) ?: return
        viewModel.exportHistory(applicationContext, fileName, exportType)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        ExportHistoryUiState.Idle -> {
                            isExporting = false
                            showLoading(false)
                        }

                        ExportHistoryUiState.Loading -> {
                            isExporting = true
                            showLoading(true)
                        }

                        ExportHistoryUiState.Success -> {
                            isExporting = false
                            showLoading(false)
                            showHistoryExported()
                        }

                        is ExportHistoryUiState.Error -> {
                            isExporting = false
                            showLoading(false)
                            showError(state.throwable)
                        }
                    }
                    updateExportButtonState()
                }
            }
        }
    }

    private fun updateExportButtonState() {
        val isFileNameValid = binding.editTextFileName.isNotBlank()
        binding.buttonExport.isEnabled = isFileNameValid && isExporting.not()
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