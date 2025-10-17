package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeEmailBinding
import com.d4rk.qrcodescanner.plus.model.schema.Email
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateButtonStateController
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodeEmailFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeEmailBinding
    private lateinit var buttonStateController: CreateButtonStateController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodeEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        initButtonStateController()
    }

    override fun getBarcodeSchema(): Schema {
        return Email(
            email = binding.editTextEmail.textString,
            subject = binding.editTextSubject.textString,
            body = binding.editTextMessage.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextEmail.requestFocus()
    }

    private fun initButtonStateController() {
        buttonStateController = CreateButtonStateController(this) { fields ->
            fields.any { it.isNotBlank() }
        }
        buttonStateController.bind(
            viewLifecycleOwner,
            binding.editTextEmail,
            binding.editTextSubject,
            binding.editTextMessage
        )
    }
}