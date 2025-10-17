package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeSmsBinding
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.model.schema.Sms
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateButtonStateController
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodeSmsFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeSmsBinding
    private lateinit var buttonStateController: CreateButtonStateController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodeSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        initButtonStateController()
    }

    override fun showPhone(phone: String) {
        binding.editTextPhone.apply {
            setText(phone)
            setSelection(phone.length)
        }
        if (::buttonStateController.isInitialized) {
            buttonStateController.refresh()
        }
    }

    override fun getBarcodeSchema(): Schema {
        return Sms(
            phone = binding.editTextPhone.textString, message = binding.editTextMessage.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextPhone.requestFocus()
    }

    private fun initButtonStateController() {
        buttonStateController = CreateButtonStateController(this) { fields ->
            fields.any { it.isNotBlank() }
        }
        buttonStateController.bind(
            viewLifecycleOwner,
            binding.editTextPhone,
            binding.editTextMessage
        )
    }
}