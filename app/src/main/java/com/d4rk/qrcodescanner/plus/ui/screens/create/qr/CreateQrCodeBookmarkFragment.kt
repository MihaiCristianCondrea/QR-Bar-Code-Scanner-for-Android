package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeBookmarkBinding
import com.d4rk.qrcodescanner.plus.model.schema.Bookmark
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodeBookmarkFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeBookmarkBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodeBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        handleTextChanged()
    }

    override fun getBarcodeSchema(): Schema {
        return Bookmark(
            title = binding.editTextTitle.textString, url = binding.editTextUrl.textString
        )
    }

    private fun initTitleEditText() {
        binding.editTextTitle.requestFocus()
    }

    private fun handleTextChanged() {
        binding.editTextTitle.addTextChangedListener { toggleCreateBarcodeButton() }
        binding.editTextUrl.addTextChangedListener { toggleCreateBarcodeButton() }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled =
            binding.editTextTitle.isNotBlank() || binding.editTextUrl.isNotBlank()
    }
}