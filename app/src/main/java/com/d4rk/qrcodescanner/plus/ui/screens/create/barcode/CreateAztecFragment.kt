package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateAztecBinding
import com.d4rk.qrcodescanner.plus.extension.textString
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.model.schema.Other
import com.d4rk.qrcodescanner.plus.model.schema.Schema
class CreateAztecFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateAztecBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding =  FragmentCreateAztecBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editText.requestFocus()
    }
    override fun getBarcodeSchema(): Schema {
        return Other(binding.editText.textString)
    }
}