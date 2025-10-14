package com.d4rk.qrcodescanner.plus.ui.screens.settings.formats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.databinding.ActivitySupportedFormatsBinding
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.domain.scan.SupportedBarcodeFormats
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.zxing.BarcodeFormat
import org.koin.android.ext.android.inject

class SupportedFormatsActivity : BaseActivity(), FormatsAdapter.Listener {
    private lateinit var binding: ActivitySupportedFormatsBinding
    private val settings: Settings by inject()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SupportedFormatsActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val formats by unsafeLazy { SupportedBarcodeFormats.FORMATS }
    private val formatSelection by unsafeLazy { formats.map(settings::isFormatSelected) }
    private val formatsAdapter by unsafeLazy { FormatsAdapter(this, formats, formatSelection) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportedFormatsBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        initRecyclerView()
    }

    override fun onFormatChecked(format: BarcodeFormat, isChecked: Boolean) {
        settings.setFormatSelected(format, isChecked)
    }

    private fun initRecyclerView() {
        binding.recyclerViewFormats.apply {
            layoutManager = LinearLayoutManager(this@SupportedFormatsActivity)
            adapter = formatsAdapter
        }
    }
}