package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodePhoneBinding
import com.d4rk.qrcodescanner.plus.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.extension.textString
import com.d4rk.qrcodescanner.plus.model.schema.Phone
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment

class CreateQrCodePhoneFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding : FragmentCreateQrCodePhoneBinding
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        binding = FragmentCreateQrCodePhoneBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        initEditText()
        handleTextChanged()
    }

    override fun showPhone(phone : String) {
        binding.editText.apply {
            setText(phone)
            setSelection(phone.length)
        }
    }

    override fun getBarcodeSchema() : Schema {
        return Phone(binding.editText.textString)
    }

    private fun initEditText() {
        binding.editText.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editText.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = binding.editText.isNotBlank()
        }
    }
}