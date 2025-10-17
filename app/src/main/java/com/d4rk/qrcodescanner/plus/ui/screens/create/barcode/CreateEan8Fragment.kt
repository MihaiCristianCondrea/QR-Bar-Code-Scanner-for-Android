package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateEan8Binding

class CreateEan8Fragment :
    SingleTextBarcodeFragment<FragmentCreateEan8Binding>(FragmentCreateEan8Binding::inflate) {

    override fun getInputField(binding: FragmentCreateEan8Binding) = binding.editText
}
