@file:Suppress("DEPRECATION")

package com.d4rk.qrcodescanner.plus.ui.screens.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.LazyThreadSafetyMode

class ScanBarcodeFromCameraFragment : Fragment(), ConfirmBarcodeDialogFragment.Listener {
    private lateinit var binding: FragmentScanBarcodeFromCameraBinding

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101
        private const val ZXING_SCAN_INTENT_ACTION = "com.google.zxing.client.android.SCAN"
        private const val CONTINUOUS_SCANNING_PREVIEW_DELAY = 500L
    }

    private val vibrationPattern = longArrayOf(0, 350)

    private var maxZoom: Int = 0
    private val zoomStep = 5
    private lateinit var codeScanner: CodeScanner
    private var lastResult: Barcode? = null
    private val touchFocusReflection by lazy(LazyThreadSafetyMode.NONE) {
        runCatching {
            val rectClass = Class.forName("com.budiyev.android.codescanner.Rect")
            val constructor = rectClass.getDeclaredConstructor(
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            ).apply {
                isAccessible = true
            }
            val method = CodeScanner::class.java.getDeclaredMethod("performTouchFocus", rectClass).apply {
                isAccessible = true
            }
            TouchFocusReflection(method, constructor)
        }.getOrNull()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentScanBarcodeFromCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportEdgeToEdge()
        initScanner()
        initFlashButton()
        handleScanFromFileClicked()
        setupManualFocus()
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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onBarcodeConfirmed(barcode: Barcode) {
        handleConfirmedBarcode(barcode)
    }

    override fun onBarcodeDeclined() {
        restartPreview()
    }

    private fun supportEdgeToEdge() {
        binding.imageViewFlash.applySystemWindowInsets(applyTop = true)
        binding.imageViewScanFromFile.applySystemWindowInsets(applyTop = true)
    }

    private fun initScanner() {
        val flashAvailable = isFlashAvailable()
        if (settings.flash && flashAvailable.not()) {
            settings.flash = false
        }
        codeScanner = CodeScanner(requireActivity(), binding.scannerView).apply {
            camera = if (settings.isBackCamera) {
                CodeScanner.CAMERA_BACK
            } else {
                CodeScanner.CAMERA_FRONT
            }
            autoFocusMode = if (settings.simpleAutoFocus) {
                AutoFocusMode.SAFE
            } else {
                AutoFocusMode.CONTINUOUS
            }
            formats = SupportedBarcodeFormats.FORMATS.filter(settings::isFormatSelected)
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = settings.flash && flashAvailable
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
        val flashAvailable = isFlashAvailable()
        binding.imageViewFlash.isVisible = flashAvailable
        binding.textViewFlash.isVisible = flashAvailable
        binding.layoutFlashContainer.isVisible = flashAvailable
        if (flashAvailable.not()) {
            return
        }
        binding.layoutFlashContainer.setOnClickListener {
            toggleFlash()
        }
        binding.imageViewFlash.isActivated = codeScanner.isFlashEnabled
    }

    private fun handleScanFromFileClicked() {
        binding.layoutScanFromFileContainer.setOnClickListener {
            navigateToScanFromFileScreen()
        }
    }

    private fun handleZoomChanged() {
        binding.seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
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
            zoom = if (zoom > zoomStep) {
                zoom - zoomStep
            } else {
                0
            }
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun increaseZoom() {
        codeScanner.apply {
            zoom = if (zoom < maxZoom - zoomStep) {
                zoom + zoomStep
            } else {
                maxZoom
            }
            binding.seekBarZoom.progress = zoom
        }
    }

    private fun handleScannedBarcode(result: Result) {
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
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun handleConfirmedBarcode(barcode: Barcode) {
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun vibrateIfNeeded() {
        if (settings.vibrate) {
            requireActivity().runOnUiThread {
                requireActivity().applicationContext.vibrator?.vibrateOnce(vibrationPattern)
            }
        }
    }

    private fun showScanConfirmationDialog(barcode: Barcode) {
        val dialog = ConfirmBarcodeDialogFragment.newInstance(barcode)
        dialog.show(childFragmentManager, "")
    }

    private fun saveScannedBarcode(barcode: Barcode) {
        lifecycleScope.launch {
            try {
                val id = barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
                lastResult = barcode
                if (settings.continuousScanning) {
                    restartPreviewWithDelay(true)
                } else {
                    navigateToBarcodeScreen(barcode.copy(id = id))
                }
            } catch (throwable: Exception) {
                showError(throwable)
            }
        }
    }

    private fun restartPreviewWithDelay(showMessage: Boolean) {
        lifecycleScope.launch {
            delay(CONTINUOUS_SCANNING_PREVIEW_DELAY)
            if (!isAdded) {
                return@launch
            }
            if (showMessage) {
                view?.let { Snackbar.make(it, R.string.saved, Snackbar.LENGTH_LONG).show() }
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
        if (isFlashAvailable().not()) {
            return
        }
        val newState = codeScanner.isFlashEnabled.not()
        runCatching {
            if (codeScanner.isPreviewActive.not()) {
                codeScanner.startPreview()
            }
            codeScanner.isFlashEnabled = newState
        }.onSuccess {
            settings.flash = newState
            binding.imageViewFlash.isActivated = newState
        }.onFailure { throwable ->
            binding.imageViewFlash.isActivated = !newState
            showError(throwable)
        }
    }

    private fun requestPermissions() {
        permissionsHelper.requestNotGrantedPermissions(
            requireActivity() as AppCompatActivity,
            PERMISSIONS,
            PERMISSION_REQUEST_CODE,
        )
    }

    private fun areAllPermissionsGranted(): Boolean {
        return permissionsHelper.areAllPermissionsGranted(requireActivity(), PERMISSIONS)
    }

    private fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        return permissionsHelper.areAllPermissionsGranted(grantResults)
    }

    private fun navigateToScanFromFileScreen() {
        ScanBarcodeFromFileActivity.start(requireActivity())
    }

    private fun navigateToBarcodeScreen(barcode: Barcode) {
        BarcodeActivity.start(requireActivity(), barcode)
    }

    private fun finishWithResult(result: Result) {
        val intent = Intent()
            .putExtra("SCAN_RESULT", result.text)
            .putExtra("SCAN_RESULT_FORMAT", result.barcodeFormat.toString())
        if (result.rawBytes?.isNotEmpty() == true) {
            intent.putExtra("SCAN_RESULT_BYTES", result.rawBytes)
        }
        result.resultMetadata?.let { metadata ->
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_ORIENTATION", it.toString())
            }
            metadata[ResultMetadataType.ERROR_CORRECTION_LEVEL]?.let {
                intent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL", it.toString())
            }
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_UPC_EAN_EXTENSION", it.toString())
            }
            metadata[ResultMetadataType.BYTE_SEGMENTS]?.let {
                @Suppress("UNCHECKED_CAST")
                for ((index, segment) in (it as Iterable<ByteArray>).withIndex()) {
                    intent.putExtra("SCAN_RESULT_BYTE_SEGMENTS_$index", segment)
                }
            }
        }
        requireActivity().apply {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupManualFocus() {
        val reflection = touchFocusReflection ?: return
        binding.scannerView.setOnTouchListener { view, event ->
            if (event.action != MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener false
            }
            val handled = focusOnCoordinates(reflection, event.x, event.y)
            if (handled) {
                view.performClick()
            }
            handled
        }
    }

    private fun focusOnCoordinates(reflection: TouchFocusReflection, x: Float, y: Float): Boolean {
        if (codeScanner.isPreviewActive.not()) {
            return false
        }
        val viewWidth = binding.scannerView.width
        val viewHeight = binding.scannerView.height
        if (viewWidth <= 0 || viewHeight <= 0) {
            return false
        }
        val radius = resources.getDimensionPixelSize(R.dimen.manual_focus_area_radius)
        val left = (x.toInt() - radius).coerceAtLeast(0)
        val top = (y.toInt() - radius).coerceAtLeast(0)
        val right = (x.toInt() + radius).coerceAtMost(viewWidth)
        val bottom = (y.toInt() + radius).coerceAtMost(viewHeight)
        if (left >= right || top >= bottom) {
            return false
        }
        val rect = runCatching {
            reflection.rectConstructor.newInstance(left, top, right, bottom)
        }.getOrElse {
            return false
        }
        return runCatching {
            reflection.method.invoke(codeScanner, rect)
        }.isSuccess
    }

    private fun isFlashAvailable(): Boolean {
        val context = context ?: return false
        val packageManager = context.packageManager
        val hasFlash = packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) == true
        return hasFlash && settings.isBackCamera
    }

    private data class TouchFocusReflection(
        val method: Method,
        val rectConstructor: Constructor<*>,
    )
}
