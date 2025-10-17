package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreatePdf417Binding

class CreatePdf417Fragment :
    SingleTextBarcodeFragment<FragmentCreatePdf417Binding>(FragmentCreatePdf417Binding::inflate) {

    override fun getInputField(binding: FragmentCreatePdf417Binding) = binding.editText
}
