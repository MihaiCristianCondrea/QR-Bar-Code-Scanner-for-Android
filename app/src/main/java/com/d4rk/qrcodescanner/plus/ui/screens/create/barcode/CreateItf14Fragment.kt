package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateItf14Binding

class CreateItf14Fragment :
    SingleTextBarcodeFragment<FragmentCreateItf14Binding>(FragmentCreateItf14Binding::inflate) {

    override fun getInputField(binding: FragmentCreateItf14Binding) = binding.editText
}
