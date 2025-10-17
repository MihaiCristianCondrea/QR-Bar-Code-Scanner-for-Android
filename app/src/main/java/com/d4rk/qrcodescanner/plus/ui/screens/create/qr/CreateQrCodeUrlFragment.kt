package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeUrlBinding
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.model.schema.Url
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateButtonStateController
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodeUrlFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeUrlBinding
    private lateinit var buttonStateController: CreateButtonStateController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodeUrlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showUrlPrefix()
        initButtonStateController()
    }

    override fun getBarcodeSchema(): Schema {
        return Url(binding.editText.textString)
    }

    private fun showUrlPrefix() {
        val prefix = "https://"
        binding.editText.apply {
            setText(prefix)
            setSelection(prefix.length)
            requestFocus()
        }
    }

    private fun initButtonStateController() {
        buttonStateController = CreateButtonStateController(this) { fields ->
            fields.any { it.isNotBlank() }
        }
        buttonStateController.bind(viewLifecycleOwner, binding.editText)
    }
}