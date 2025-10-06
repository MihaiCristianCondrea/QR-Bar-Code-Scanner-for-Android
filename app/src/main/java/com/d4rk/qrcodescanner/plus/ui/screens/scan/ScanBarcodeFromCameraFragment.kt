@file:Suppress("DEPRECATION")

package com.d4rk.qrcodescanner.plus.ui.screens.scan

// Removed: import io.reactivex.disposables.CompositeDisposable
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentScanBarcodeFromCameraBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeParser
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.di.scannerCameraHelper
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.domain.scan.SupportedBarcodeFormats
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.extension.equalTo
import com.d4rk.qrcodescanner.plus.extension.showError
import com.d4rk.qrcodescanner.plus.extension.vibrateOnce
import com.d4rk.qrcodescanner.plus.extension.vibrator
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.ConfirmBarcodeDialogFragment
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.ui.screens.scan.file.ScanBarcodeFromFileActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanBarcodeFromCameraFragment : Fragment() , ConfirmBarcodeDialogFragment.Listener {
    private lateinit var binding : FragmentScanBarcodeFromCameraBinding

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101
        private const val ZXING_SCAN_INTENT_ACTION = "com.google.zxing.client.android.SCAN"
        private const val CONTINUOUS_SCANNING_PREVIEW_DELAY = 500L
    }

    private val vibrationPattern = arrayOf<Long>(0 , 350).toLongArray()

    private var maxZoom : Int = 0
    private val zoomStep = 5
    private lateinit var codeScanner : CodeScanner
    private var lastResult : Barcode? = null
    override fun onCreateView(
        inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?
    ) : View {
        binding = FragmentScanBarcodeFromCameraBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        supportEdgeToEdge()
        initScanner()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (areAllPermissionsGranted()) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode : Int , permissions : Array<out String> , grantResults : IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    override fun onBarcodeConfirmed(barcode : Barcode) {
        handleConfirmedBarcode(barcode)
    }

    override fun onBarcodeDeclined() {
        restartPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun supportEdgeToEdge() {
        binding.imageViewFlash.applySystemWindowInsets(applyTop = true)
        binding.imageViewScanFromFile.applySystemWindowInsets(applyTop = true)
    }

    private fun initScanner() {
        codeScanner = CodeScanner(requireActivity() , binding.scannerView).apply {
            camera = if (settings.isBackCamera) {
                CodeScanner.CAMERA_BACK
            }
            else {
                CodeScanner.CAMERA_FRONT
            }
            autoFocusMode = if (settings.simpleAutoFocus) {
                AutoFocusMode.SAFE
            }
            else {
                AutoFocusMode.CONTINUOUS
            }
            formats = SupportedBarcodeFormats.FORMATS.filter(settings::isFormatSelected)
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = settings.flash
            isTouchFocusEnabled = true
            decodeCallback = DecodeCallback(::handleScannedBarcode)
            errorCallback = ErrorCallback(::showError)
        }
    }

    private fun initZoomSeekBar() {
        scannerCameraHelper.getCameraParameters(settings.isBackCamera)?.apply {
            this@ScanBarcodeFromCameraFragment.maxZoom = maxZoom
            binding.seekBarZoom.max = maxZoom
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun initFlashButton() {
        binding.layoutFlashContainer.setOnClickListener {
            toggleFlash()
        }
        binding.imageViewFlash.isActivated = settings.flash
    }

    private fun handleScanFromFileClicked() {
        binding.layoutScanFromFileContainer.setOnClickListener {
            navigateToScanFromFileScreen()
        }
    }

    private fun handleZoomChanged() {
        binding.seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar : SeekBar?) {}
            override fun onStopTrackingTouch(seekBar : SeekBar?) {}
            override fun onProgressChanged(seekBar : SeekBar? , progress : Int , fromUser : Boolean) {
                if (fromUser) {
                    codeScanner.zoom = progress
                }
            }
        })
    }

    private fun handleDecreaseZoomClicked() {
        binding.buttonDecreaseZoom.setOnClickListener {
            decreaseZoom()
        }
    }

    private fun handleIncreaseZoomClicked() {
        binding.buttonIncreaseZoom.setOnClickListener {
            increaseZoom()
        }
    }

    private fun decreaseZoom() {
        codeScanner.apply {
            if (zoom > zoomStep) {
                zoom -= zoomStep
            }
            else {
                zoom = 0
            }
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun increaseZoom() {
        codeScanner.apply {
            if (zoom < maxZoom - zoomStep) {
                zoom += zoomStep
            }
            else {
                zoom = maxZoom
            }
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun handleScannedBarcode(result : Result) {
        if (requireActivity().intent?.action == ZXING_SCAN_INTENT_ACTION) {
            vibrateIfNeeded()
            finishWithResult(result)
            return
        }
        if (settings.continuousScanning && result.equalTo(lastResult)) {
            restartPreviewWithDelay(false)
            return
        }
        vibrateIfNeeded()
        val barcode = barcodeParser.parseResult(result)
        when {
            settings.confirmScansManually -> showScanConfirmationDialog(barcode)
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(
                barcode
            )

            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun handleConfirmedBarcode(barcode : Barcode) {
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(
                barcode
            )

            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun vibrateIfNeeded() {
        if (settings.vibrate) {
            requireActivity().apply {
                runOnUiThread {
                    applicationContext.vibrator?.vibrateOnce(vibrationPattern)
                }
            }
        }
    }

    private fun showScanConfirmationDialog(barcode : Barcode) {
        val dialog = ConfirmBarcodeDialogFragment.newInstance(barcode)
        dialog.show(childFragmentManager , "")
    }

    private fun saveScannedBarcode(barcode : Barcode) {
        lifecycleScope.launch {
            try {
                val id = barcodeDatabase.save(
                    barcode , settings.doNotSaveDuplicates
                ) // Assuming this is a suspend function
                lastResult = barcode
                when (settings.continuousScanning) {
                    true -> restartPreviewWithDelay(true)
                    else -> navigateToBarcodeScreen(barcode.copy(id = id))
                }
            } catch (e : Exception) {
                showError(e)
            }
        }
    }

    private fun restartPreviewWithDelay(showMessage : Boolean) {
        lifecycleScope.launch {
            delay(CONTINUOUS_SCANNING_PREVIEW_DELAY)
            if (showMessage) {
                Snackbar.make(requireView() , R.string.saved , Snackbar.LENGTH_LONG).show()
            }
            restartPreview()
        }
    }

    private fun restartPreview() {
        requireActivity().runOnUiThread {
            codeScanner.startPreview()
        }
    }

    private fun toggleFlash() {
        binding.imageViewFlash.isActivated = binding.imageViewFlash.isActivated.not()
        codeScanner.isFlashEnabled = codeScanner.isFlashEnabled.not()
    }

    private fun requestPermissions() {
        permissionsHelper.requestNotGrantedPermissions(
            requireActivity() as AppCompatActivity , PERMISSIONS , PERMISSION_REQUEST_CODE
        )
    }

    private fun areAllPermissionsGranted() : Boolean {
        return permissionsHelper.areAllPermissionsGranted(requireActivity() , PERMISSIONS)
    }

    private fun areAllPermissionsGranted(grantResults : IntArray) : Boolean {
        return permissionsHelper.areAllPermissionsGranted(grantResults)
    }

    private fun navigateToScanFromFileScreen() {
        ScanBarcodeFromFileActivity.start(requireActivity())
    }

    private fun navigateToBarcodeScreen(barcode : Barcode) {
        BarcodeActivity.start(requireActivity() , barcode)
    }

    private fun finishWithResult(result : Result) {
        val intent = Intent().putExtra("SCAN_RESULT" , result.text).putExtra("SCAN_RESULT_FORMAT" , result.barcodeFormat.toString())
        if (result.rawBytes?.isNotEmpty() == true) {
            intent.putExtra("SCAN_RESULT_BYTES" , result.rawBytes)
        }
        result.resultMetadata?.let { metadata ->
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_ORIENTATION" , it.toString())
            }
            metadata[ResultMetadataType.ERROR_CORRECTION_LEVEL]?.let {
                intent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL" , it.toString())
            }
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_UPC_EAN_EXTENSION" , it.toString())
            }
            metadata[ResultMetadataType.BYTE_SEGMENTS]?.let {
                @Suppress("UNCHECKED_CAST") for ((i , seg) in (it as Iterable<ByteArray>).withIndex()) {
                    intent.putExtra("SCAN_RESULT_BYTE_SEGMENTS_$i" , seg)
                }
            }
        }
        requireActivity().apply {
            setResult(Activity.RESULT_OK , intent)
            finish()
        }
    }
}