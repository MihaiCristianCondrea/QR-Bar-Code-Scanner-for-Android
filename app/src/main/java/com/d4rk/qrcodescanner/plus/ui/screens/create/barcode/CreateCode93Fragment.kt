package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateCode93Binding

class CreateCode93Fragment :
    SingleTextBarcodeFragment<FragmentCreateCode93Binding>(FragmentCreateCode93Binding::inflate) {

    override fun getInputField(binding: FragmentCreateCode93Binding) = binding.editText
}
