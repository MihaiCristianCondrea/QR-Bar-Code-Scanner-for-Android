package com.d4rk.qrcodescanner.plus.ui.screens.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentScanBarcodeFromCameraBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeParser
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.domain.scan.SupportedBarcodeFormats
import com.d4rk.qrcodescanner.plus.utils.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.extension.toGmsFormat
import com.d4rk.qrcodescanner.plus.utils.extension.vibrateOnce
import com.d4rk.qrcodescanner.plus.utils.extension.vibrator
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.ConfirmBarcodeDialogFragment
import com.d4rk.qrcodescanner.plus.ui.components.views.BarcodeOverlayView
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.ui.screens.scan.file.ScanBarcodeFromFileActivity
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.common.Barcode as MlKitBarcode

class ScanBarcodeFromCameraFragment : Fragment(), ConfirmBarcodeDialogFragment.Listener {

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101
        private const val ZXING_SCAN_INTENT_ACTION = "com.google.zxing.client.android.SCAN"
        private const val CONTINUOUS_SCANNING_PREVIEW_DELAY = 500L
        private const val ZOOM_MAX_PROGRESS = 100
    }

    private lateinit var binding: FragmentScanBarcodeFromCameraBinding

    private val vibrationPattern = longArrayOf(0, 350)
    private val zoomStep = 5

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null
    private var camera: Camera? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var isHandlingResult = false
    private var lastResult: Barcode? = null
    private var pendingBarcode: Barcode? = null
    private var currentZoomProgress: Int = 0
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

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
        lensFacing = if (settings.isBackCamera) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        supportEdgeToEdge()
        initUi()
        setupCameraProvider()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (areAllPermissionsGranted()) {
            bindCameraUseCases()
        }
    }

    override fun onPause() {
        pauseCamera()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pauseCamera()
        cameraExecutor?.shutdown()
        cameraExecutor = null
        barcodeScanner?.close()
        barcodeScanner = null
        cameraProvider = null
    }

    override fun onRequestPermissionsResult( // FIXME: This declaration overrides a deprecated member but is not marked as deprecated itself. Add the '@Deprecated' annotation or suppress the diagnostic.
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            bindCameraUseCases()
        }
    }

    override fun onBarcodeConfirmed(barcode: Barcode) {
        pendingBarcode = null
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> {
                lastResult = barcode
                navigateToBarcodeScreen(barcode)
            }
        }
    }

    override fun onBarcodeDeclined() {
        pendingBarcode = null
        binding.barcodeOverlay.clear()
        isHandlingResult = false
    }

    private fun initUi() {
        binding.seekBarZoom.valueTo = ZOOM_MAX_PROGRESS.toFloat()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
        setupTapToFocus()
    }

    private fun supportEdgeToEdge() {
        binding.imageViewFlash.applySystemWindowInsets(applyTop = true)
        binding.imageViewScanFromFile.applySystemWindowInsets(applyTop = true)
    }

    private fun setupCameraProvider() {
        val providerFuture = ProcessCameraProvider.getInstance(requireContext())
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
            if (areAllPermissionsGranted()) {
                bindCameraUseCases()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        if (!areAllPermissionsGranted()) {
            return
        }
        runCatching {
            val provider = cameraProvider ?: return@runCatching
            val executor = cameraExecutor ?: return@runCatching
            val preview = Preview.Builder().build().apply {
                surfaceProvider = binding.previewView.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val scanner = createBarcodeScanner()
            analysis.setAnalyzer(executor) { imageProxy -> processImage(scanner, imageProxy) }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            provider.unbindAll()
            barcodeScanner?.close()
            barcodeScanner = scanner
            camera = provider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, analysis)
            camera?.let {
                observeTorchState()
                observeZoomState()
                updateFlashAvailability()
                applyInitialTorchState()
            }
        }.onFailure(::showError)
    }

    private fun pauseCamera() {
        cameraProvider?.unbindAll()
        camera = null
        binding.barcodeOverlay.clear()
        isHandlingResult = false
        pendingBarcode = null
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(scanner: BarcodeScanner, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        binding.barcodeOverlay.post {
            binding.barcodeOverlay.setImageSourceInfo(
                imageProxy.width,
                imageProxy.height,
                imageProxy.imageInfo.rotationDegrees,
                lensFacing == CameraSelector.LENS_FACING_FRONT,
            )
        }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                handleDetectedBarcodes(barcodes)
            }
            .addOnFailureListener { throwable ->
                showError(throwable)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleDetectedBarcodes(barcodes: List<MlKitBarcode>) {
        if (!isAdded) {
            return
        }
        if (barcodes.isEmpty()) {
            binding.barcodeOverlay.clear()
            return
        }
        val shapes = barcodes.mapNotNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@mapNotNull null
            val cornerPoints = barcode.cornerPoints?.map { point ->
                PointF(point.x.toFloat(), point.y.toFloat())
            } ?: emptyList()
            BarcodeOverlayView.BarcodeShape(boundingBox, cornerPoints)
        }
        binding.barcodeOverlay.update(shapes)
        val parsedBarcode = barcodes.firstNotNullOfOrNull(barcodeParser::parse) ?: return
        if (settings.continuousScanning && parsedBarcode == lastResult) {
            scheduleResumeScanning(showMessage = false)
            return
        }
        if (isHandlingResult) {
            return
        }
        isHandlingResult = true
        pendingBarcode = parsedBarcode
        handleScannedBarcode(parsedBarcode)
    }

    private fun handleScannedBarcode(barcode: Barcode) {
        if (requireActivity().intent?.action == ZXING_SCAN_INTENT_ACTION) {
            vibrateIfNeeded()
            finishWithResult(barcode)
            return
        }
        vibrateIfNeeded()
        when {
            settings.confirmScansManually -> showScanConfirmationDialog(barcode)
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> {
                lastResult = barcode
                pendingBarcode = null
                navigateToBarcodeScreen(barcode)
            }
        }
    }

    private fun saveScannedBarcode(barcode: Barcode) {
        lifecycleScope.launchWhenStarted { // FIXME: 'fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit): Job' is deprecated. launchWhenStarted is deprecated as it can lead to wasted resources in some cases. Replace with suspending repeatOnLifecycle to run the block whenever the Lifecycle state is at least Lifecycle.State.STARTED.
            try {
                val id = barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
                val savedBarcode = barcode.copy(id = id)
                lastResult = barcode
                pendingBarcode = null
                if (settings.continuousScanning) {
                    scheduleResumeScanning(showMessage = true)
                } else {
                    navigateToBarcodeScreen(savedBarcode)
                }
            } catch (throwable: Exception) {
                isHandlingResult = false
                pendingBarcode = null
                showError(throwable)
            }
        }
    }

    private fun scheduleResumeScanning(showMessage: Boolean) {
        lifecycleScope.launchWhenStarted { // FIXME: 'fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit): Job' is deprecated. launchWhenStarted is deprecated as it can lead to wasted resources in some cases. Replace with suspending repeatOnLifecycle to run the block whenever the Lifecycle state is at least Lifecycle.State.STARTED.
            if (!isAdded) {
                return@launchWhenStarted
            }
            delay(CONTINUOUS_SCANNING_PREVIEW_DELAY)
            if (!isAdded) {
                return@launchWhenStarted
            }
            if (showMessage) {
                view?.let { Snackbar.make(it, R.string.saved, Snackbar.LENGTH_LONG).show() }
            }
            binding.barcodeOverlay.clear()
            pendingBarcode = null
            isHandlingResult = false
        }
    }

    private fun showScanConfirmationDialog(barcode: Barcode) {
        val dialog = ConfirmBarcodeDialogFragment.newInstance(barcode)
        dialog.show(childFragmentManager, "")
    }

    private fun vibrateIfNeeded() {
        if (settings.vibrate) {
            requireActivity().runOnUiThread {
                requireActivity().applicationContext.vibrator?.vibrateOnce(vibrationPattern)
            }
        }
    }

    private fun finishWithResult(barcode: Barcode) {
        pendingBarcode = null
        val intent = Intent()
            .putExtra("SCAN_RESULT", barcode.text)
            .putExtra("SCAN_RESULT_FORMAT", barcode.format.toString())
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    private fun navigateToBarcodeScreen(barcode: Barcode) {
        BarcodeActivity.start(requireActivity(), barcode)
    }

    private fun handleScanFromFileClicked() {
        binding.layoutScanFromFileContainer.setOnClickListener {
            ScanBarcodeFromFileActivity.start(requireActivity())
        }
    }

    private fun initFlashButton() {
        binding.layoutFlashContainer.setOnClickListener {
            toggleFlash()
        }
    }

    private fun toggleFlash() {
        val camera = camera ?: return
        val flashAvailable = camera.cameraInfo.hasFlashUnit() && lensFacing == CameraSelector.LENS_FACING_BACK
        if (flashAvailable.not()) {
            return
        }
        val enableTorch = binding.imageViewFlash.isActivated.not()
        camera.cameraControl.enableTorch(enableTorch)
        settings.flash = enableTorch
    }

    private fun updateFlashAvailability() {
        val camera = camera
        val flashAvailable = camera?.cameraInfo?.hasFlashUnit() == true && lensFacing == CameraSelector.LENS_FACING_BACK
        binding.imageViewFlash.isVisible = flashAvailable
        binding.imageViewFlash.isVisible = flashAvailable
        binding.layoutFlashContainer.isVisible = flashAvailable
        if (flashAvailable.not()) {
            settings.flash = false
        }
    }

    private fun applyInitialTorchState() {
        val camera = camera ?: return
        val flashAvailable = camera.cameraInfo.hasFlashUnit() && lensFacing == CameraSelector.LENS_FACING_BACK
        if (flashAvailable) {
            camera.cameraControl.enableTorch(settings.flash)
        } else {
            settings.flash = false
        }
    }

    private fun observeTorchState() {
        val camera = camera ?: return
        camera.cameraInfo.torchState.observe(viewLifecycleOwner) { state ->
            binding.imageViewFlash.isActivated = state == TorchState.ON
        }
    }

    private fun observeZoomState() {
        val camera = camera ?: return
        camera.cameraInfo.zoomState.observe(viewLifecycleOwner) { zoomState ->
            val progress = (zoomState.linearZoom * ZOOM_MAX_PROGRESS).toInt().coerceIn(0, ZOOM_MAX_PROGRESS)
            currentZoomProgress = progress.also { binding.seekBarZoom.value = it.toFloat() }
        }
    }

    private fun handleZoomChanged() {
        binding.seekBarZoom.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                setLinearZoom(value.toInt())
            }
        }
    }

    private fun handleDecreaseZoomClicked() {
        binding.buttonDecreaseZoom.setOnClickListener {
            val newProgress = (currentZoomProgress - zoomStep).coerceAtLeast(0)
            binding.seekBarZoom.value = newProgress.toFloat()
            setLinearZoom(newProgress)
        }
    }

    private fun handleIncreaseZoomClicked() {
        binding.buttonIncreaseZoom.setOnClickListener {
            val newProgress = (currentZoomProgress + zoomStep).coerceAtMost(ZOOM_MAX_PROGRESS)
            binding.seekBarZoom.value = newProgress.toFloat()
            setLinearZoom(newProgress)
        }
    }

    private fun setLinearZoom(progress: Int) {
        currentZoomProgress = progress
        camera?.cameraControl?.setLinearZoom(progress.toFloat() / ZOOM_MAX_PROGRESS)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTapToFocus() {
        binding.previewView.setOnTouchListener { view, event ->
            if (event.action != MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener false
            }
            val camera = camera ?: return@setOnTouchListener false
            val meteringPoint = binding.previewView.meteringPointFactory.createPoint(event.x, event.y)
            val focusAction = FocusMeteringAction.Builder(meteringPoint).build()
            camera.cameraControl.startFocusAndMetering(focusAction)
            view.performClick()
            true
        }
    }

    private fun createBarcodeScanner(): BarcodeScanner {
        val selectedFormats = SupportedBarcodeFormats.FORMATS
            .filter(settings::isFormatSelected)
            .mapNotNull { it.toGmsFormat() }
        val options = BarcodeScannerOptions.Builder().apply {
            if (selectedFormats.isNotEmpty()) {
                val first = selectedFormats.first()
                val others = selectedFormats.drop(1).toIntArray()
                setBarcodeFormats(first, *others)
            }
        }.build()
        return BarcodeScanning.getClient(options)
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
}
