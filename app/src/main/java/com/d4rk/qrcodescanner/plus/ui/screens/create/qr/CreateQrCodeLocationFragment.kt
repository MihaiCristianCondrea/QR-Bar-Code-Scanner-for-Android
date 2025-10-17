package com.d4rk.qrcodescanner.plus.ui.screens.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.d4rk.qrcodescanner.plus.databinding.FragmentCreateQrCodeLocationBinding
import com.d4rk.qrcodescanner.plus.model.schema.Geo
import com.d4rk.qrcodescanner.plus.model.schema.Schema
import com.d4rk.qrcodescanner.plus.ui.screens.create.BaseCreateBarcodeFragment
import com.d4rk.qrcodescanner.plus.ui.screens.create.CreateButtonStateController
import com.d4rk.qrcodescanner.plus.utils.extension.isNotBlank
import com.d4rk.qrcodescanner.plus.utils.extension.textString

class CreateQrCodeLocationFragment : BaseCreateBarcodeFragment() {
    private lateinit var binding: FragmentCreateQrCodeLocationBinding
    private lateinit var buttonStateController: CreateButtonStateController
    override val latitude: Double? get() = binding.editTextLatitude.textString.toDoubleOrNull()
    override val longitude: Double? get() = binding.editTextLongitude.textString.toDoubleOrNull()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQrCodeLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLatitudeEditText()
        initButtonStateController()
    }

    override fun getBarcodeSchema(): Schema {
        return Geo(
            latitude = binding.editTextLatitude.textString,
            longitude = binding.editTextLongitude.textString,
            altitude = binding.editTextAltitude.textString
        )
    }

    override fun showLocation(latitude: Double?, longitude: Double?) {
        latitude?.apply {
            binding.editTextLatitude.setText(latitude.toString())
        }
        longitude?.apply {
            binding.editTextLongitude.setText(longitude.toString())
        }
        if (::buttonStateController.isInitialized) {
            buttonStateController.refresh()
        }
    }

    private fun initLatitudeEditText() {
        binding.editTextLatitude.requestFocus()
    }

    private fun initButtonStateController() {
        buttonStateController = CreateButtonStateController(this) { fields ->
            fields.all { it.isNotBlank() }
        }
        buttonStateController.bind(
            viewLifecycleOwner,
            binding.editTextLatitude,
            binding.editTextLongitude
        )
    }
}