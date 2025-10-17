package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateAztecBinding

class CreateAztecFragment :
    SingleTextBarcodeFragment<FragmentCreateAztecBinding>(FragmentCreateAztecBinding::inflate) {

    override fun getInputField(binding: FragmentCreateAztecBinding) = binding.editText
}
