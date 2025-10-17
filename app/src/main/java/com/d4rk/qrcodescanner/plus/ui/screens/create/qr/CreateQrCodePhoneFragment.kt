package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodePhoneBinding
import com.d4rk.qrcodescanner.plus.model.schema.Phone
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateButtonStateController
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodePhoneFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodePhoneBinding
    private lateinit var buttonStateController: CreateButtonStateController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodePhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditText()
        initButtonStateController()
    }

    override fun showPhone(phone: String) {
        binding.editText.apply {
            setText(phone)
            setSelection(phone.length)
        }
        if (::buttonStateController.isInitialized) {
            buttonStateController.refresh()
        }
    }

    override fun getBarcodeSchema(): Schema {
        return Phone(binding.editText.textString)
    }

    private fun initEditText() {
        binding.editText.requestFocus()
    }

    private fun initButtonStateController() {
        buttonStateController = CreateButtonStateController(this) { fields ->
            fields.any { it.isNotBlank() }
        }
        buttonStateController.bind(viewLifecycleOwner, binding.editText)
    }
}