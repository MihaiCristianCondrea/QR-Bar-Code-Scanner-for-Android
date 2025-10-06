package com.d4rk.qrcodescanner.plus.ui.screens.settings.formats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.d4rk.qrcodescanner.plus.databinding.ActivitySupportedFormatsBinding
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.domain.scan.SupportedBarcodeFormats
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.google.zxing.BarcodeFormat

class SupportedFormatsActivity : BaseActivity() , FormatsAdapter.Listener {
    private lateinit var binding : ActivitySupportedFormatsBinding

    companion object {
        fun start(context : Context) {
            val intent = Intent(context , SupportedFormatsActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val formats by unsafeLazy { SupportedBarcodeFormats.FORMATS }
    private val formatSelection by unsafeLazy { formats.map(settings::isFormatSelected) }
    private val formatsAdapter by unsafeLazy { FormatsAdapter(this , formats , formatSelection) }
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportedFormatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        initRecyclerView()
    }

    override fun onFormatChecked(format : BarcodeFormat , isChecked : Boolean) {
        settings.setFormatSelected(format , isChecked)
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true , applyBottom = true)
    }

    private fun initRecyclerView() {
        binding.recyclerViewFormats.apply {
            layoutManager = LinearLayoutManager(this@SupportedFormatsActivity)
            adapter = formatsAdapter
        }
    }
}