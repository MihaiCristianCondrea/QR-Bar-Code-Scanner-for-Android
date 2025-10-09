package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeCryptocurrencyBinding
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString
import com.d4rk.qrcodescanner.plus.model.schema.Cryptocurrency
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment

class CreateQrCodeCryptocurrencyFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding : FragmentCreateQrCodeCryptocurrencyBinding
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        binding = FragmentCreateQrCodeCryptocurrencyBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        initCryptocurrenciesSpinner()
        initAddressEditText()
        handleTextChanged()
    }

    override fun getBarcodeSchema() : Schema {
        val cryptocurrency = when (binding.spinnerCryptocurrency.selectedItemPosition) {
            0 -> "bitcoin"
            1 -> "bitcoincash"
            2 -> "ethereum"
            3 -> "litecoin"
            4 -> "dash"
            else -> "bitcoin"
        }
        return Cryptocurrency(
            cryptocurrency = cryptocurrency , address = binding.editTextAddress.textString , label = binding.editTextLabel.textString , amount = binding.editTextAmount.textString , message = binding.editTextMessage.textString
        )
    }

    private fun initCryptocurrenciesSpinner() {
        binding.spinnerCryptocurrency.adapter = ArrayAdapter.createFromResource(
            requireContext() , R.array.fragment_create_qr_code_cryptocurrencies , R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }

    private fun initAddressEditText() {
        binding.editTextAddress.requestFocus()
    }

    private fun handleTextChanged() {
        listOf(binding.editTextAddress , binding.editTextAmount , binding.editTextLabel , binding.editTextMessage).forEach { editText ->
            editText.addTextChangedListener { toggleCreateBarcodeButton() }
        }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = binding.editTextAddress.isNotBlank()
    }
}