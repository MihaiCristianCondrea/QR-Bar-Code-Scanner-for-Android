package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateEan13Binding

class CreateEan13Fragment :
    SingleTextBarcodeFragment<FragmentCreateEan13Binding>(FragmentCreateEan13Binding::inflate) {

    override fun getInputField(binding: FragmentCreateEan13Binding) = binding.editText
}
