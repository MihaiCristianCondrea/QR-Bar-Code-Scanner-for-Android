package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateUpcABinding

class CreateUpcAFragment :
    SingleTextBarcodeFragment<FragmentCreateUpcABinding>(FragmentCreateUpcABinding::inflate) {

    override fun getInputField(binding: FragmentCreateUpcABinding) = binding.editText
}
