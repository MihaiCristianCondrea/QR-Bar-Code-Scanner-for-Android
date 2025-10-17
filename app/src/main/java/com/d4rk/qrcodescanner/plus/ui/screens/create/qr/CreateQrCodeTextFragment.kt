package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeTextBinding
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateButtonStateController
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString
import com.google.zxing.BarcodeFormat
import org.koin.android.ext.android.inject

class CreateQrCodeTextFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeTextBinding
    private val barcodeParser: BarcodeParser by inject()
    private lateinit var buttonStateController: CreateButtonStateController

    companion object {
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"
        fun newInstance(defaultText: String): CreateQrCodeTextFragment {
            return CreateQrCodeTextFragment().apply {
                arguments = Bundle().apply {
                    putString(DEFAULT_TEXT_KEY, defaultText)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodeTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtonStateController()
        initEditText()
    }

    override fun getBarcodeSchema(): Schema {
        return barcodeParser.parseSchema(BarcodeFormat.QR_CODE, binding.editText.textString)
    }

    private fun initEditText() {
        val defaultText = arguments?.getString(DEFAULT_TEXT_KEY).orEmpty()
        binding.editText.apply {
            setText(defaultText)
            setSelection(defaultText.length)
            requestFocus()
        }
        if (::buttonStateController.isInitialized) {
            buttonStateController.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::buttonStateController.isInitialized) {
            buttonStateController.refresh()
        }
    }

    private fun initButtonStateController() {
        buttonStateController = CreateButtonStateController(this) { fields ->
            fields.any { it.isNotBlank() }
        }
        buttonStateController.bind(viewLifecycleOwner, binding.editText)
    }
}
