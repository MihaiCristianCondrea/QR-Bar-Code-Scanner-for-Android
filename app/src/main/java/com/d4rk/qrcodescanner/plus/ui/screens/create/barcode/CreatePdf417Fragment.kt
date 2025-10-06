package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreatePdf417Binding
import com.d4rk.qrcodescanner.plus.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.extension.textString
import com.d4rk.qrcodescanner.plus.model.schema.Other
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment

class CreatePdf417Fragment : BaseCreateBarcodeFragment() {
    private lateinit var binding : FragmentCreatePdf417Binding
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        binding = FragmentCreatePdf417Binding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        binding.editText.requestFocus()
        binding.editText.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = binding.editText.isNotBlank()
        }
    }

    override fun getBarcodeSchema() : Schema {
        return Other(binding.editText.textString)
    }
}