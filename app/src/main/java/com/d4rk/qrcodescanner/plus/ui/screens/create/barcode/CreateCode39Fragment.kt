package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateCode39Binding

class CreateCode39Fragment :
    SingleTextBarcodeFragment<FragmentCreateCode39Binding>(FragmentCreateCode39Binding::inflate) {

    override fun getInputField(binding: FragmentCreateCode39Binding) = binding.editText
}
