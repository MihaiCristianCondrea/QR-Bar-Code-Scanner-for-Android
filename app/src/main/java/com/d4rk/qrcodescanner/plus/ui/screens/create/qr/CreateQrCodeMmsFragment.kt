package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeMmsBinding
import com.d4rk.qrcodescanner.plus.model.schema.Mms
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodeMmsFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding : FragmentCreateQrCodeMmsBinding
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        binding = FragmentCreateQrCodeMmsBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        initTitleEditText()
        handleTextChanged()
    }

    override fun showPhone(phone : String) {
        binding.editTextPhone.apply {
            setText(phone)
            setSelection(phone.length)
        }
    }

    override fun getBarcodeSchema() : Schema {
        return Mms(
            phone = binding.editTextPhone.textString , subject = binding.editTextSubject.textString , message = binding.editTextMessage.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextPhone.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editTextPhone.addTextChangedListener { toggleCreateBarcodeButton() }
        binding.editTextSubject.addTextChangedListener { toggleCreateBarcodeButton() }
        binding.editTextMessage.addTextChangedListener { toggleCreateBarcodeButton() }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = binding.editTextPhone.isNotBlank() || binding.editTextSubject.isNotBlank() || binding.editTextMessage.isNotBlank()
    }
}