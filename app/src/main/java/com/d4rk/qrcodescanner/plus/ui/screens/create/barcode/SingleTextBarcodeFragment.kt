package com.d4rk.qrcodescanner.plus.ui.screens.create.barcode

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.viewbinding.ViewBinding
import com.d4rk.qrcodescanner.plus.model.schema.Other
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

abstract class SingleTextBarcodeFragment<T : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T
) : BaseCreateBarcodeFragment() {

    private var _binding: T? = null
    protected val binding: T
        get() = _binding ?: error("Binding accessed before being created")

    private var textWatcher: TextWatcher? = null

    protected abstract fun getInputField(binding: T): EditText

    protected open fun onBindingCreated(binding: T) {}

    protected open fun createSchema(text: String): Schema = Other(text)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBindingCreated(binding)
        val inputField = getInputField(binding)
        inputField.requestFocus()
        parentActivity.isCreateBarcodeButtonEnabled = inputField.isNotBlank()
        textWatcher = inputField.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = inputField.isNotBlank()
        }
    }

    override fun getBarcodeSchema(): Schema {
        return createSchema(getInputField(binding).textString)
    }

    override fun onDestroyView() {
        val inputField = _binding?.let { getInputField(it) }
        if (inputField != null && textWatcher != null) {
            inputField.removeTextChangedListener(textWatcher)
        }
        textWatcher = null
        _binding = null
        super.onDestroyView()
    }
}
