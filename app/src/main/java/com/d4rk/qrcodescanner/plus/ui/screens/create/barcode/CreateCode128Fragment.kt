package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateCode128Binding

class CreateCode128Fragment :
    SingleTextBarcodeFragment<FragmentCreateCode128Binding>(FragmentCreateCode128Binding::inflate) {

    override fun getInputField(binding: FragmentCreateCode128Binding) = binding.editText
}
