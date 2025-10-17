package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateCodabarBinding

class CreateCodabarFragment :
    SingleTextBarcodeFragment<FragmentCreateCodabarBinding>(FragmentCreateCodabarBinding::inflate) {

    override fun getInputField(binding: FragmentCreateCodabarBinding) = binding.editText
}
