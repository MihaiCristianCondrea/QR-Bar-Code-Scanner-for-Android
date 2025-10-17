package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateDataMatrixBinding

class CreateDataMatrixFragment :
    SingleTextBarcodeFragment<FragmentCreateDataMatrixBinding>(FragmentCreateDataMatrixBinding::inflate) {

    override fun getInputField(binding: FragmentCreateDataMatrixBinding) = binding.editText
}
