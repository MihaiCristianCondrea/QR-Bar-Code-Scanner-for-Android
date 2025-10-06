package com.d4rk.qrcodescanner.plus.ui.screens.create.qr
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeWifiBinding
import com.d4rk.qrcodescanner.plus.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.extension.textString
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.model.schema.Wifi
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
class CreateQrCodeWifiFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeWifiBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateQrCodeWifiBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEncryptionTypesSpinner()
        initNetworkNameEditText()
        handleTextChanged()
    }
    override fun getBarcodeSchema(): Schema {
        val encryption = when (binding.spinnerEncryption.selectedItemPosition) {
            0 -> "WPA"
            1 -> "WEP"
            2 -> "nopass"
            else -> "nopass"
        }
        return Wifi(
            encryption = encryption,
            name = binding.editTextNetworkName.textString,
            password = binding.editTextPassword.textString,
            isHidden = binding.checkBoxIsHidden.isChecked
        )
    }
    private fun initEncryptionTypesSpinner() {
        binding.spinnerEncryption.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.fragment_create_qr_code_wifi_encryption_types, R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        binding.spinnerEncryption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.textInputLayoutPassword.isVisible = position != 2
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
    private fun initNetworkNameEditText() {
        binding.editTextNetworkName.requestFocus()
    }
    private fun handleTextChanged() {
        binding.editTextNetworkName.addTextChangedListener { toggleCreateBarcodeButton() }
        binding.editTextPassword.addTextChangedListener { toggleCreateBarcodeButton() }
    }
    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = binding.editTextNetworkName.isNotBlank()
    }
}