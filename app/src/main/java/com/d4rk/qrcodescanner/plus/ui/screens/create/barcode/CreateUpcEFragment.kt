package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateUpcEBinding

class CreateUpcEFragment :
    SingleTextBarcodeFragment<FragmentCreateUpcEBinding>(FragmentCreateUpcEBinding::inflate) {

    override fun getInputField(binding: FragmentCreateUpcEBinding) = binding.editText
}
