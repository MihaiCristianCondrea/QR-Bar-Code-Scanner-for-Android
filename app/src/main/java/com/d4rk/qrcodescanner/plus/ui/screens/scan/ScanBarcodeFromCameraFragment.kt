package com.d4rk.qrcodescanner.plus.ui.screens.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
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
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentScanBarcodeFromCameraBinding
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.scan.SupportedBarcodeFormats
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.ConfirmBarcodeDialogFragment
import com.d4rk.qrcodescanner.plus.ui.components.views.BarcodeOverlayView
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.ui.screens.scan.file.ScanBarcodeFromFileActivity
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.extension.toGmsFormat
import com.d4rk.qrcodescanner.plus.utils.extension.vibrateOnce
import com.d4rk.qrcodescanner.plus.utils.extension.vibrator
import com.d4rk.qrcodescanner.plus.utils.helpers.PermissionsHelper
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
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
        private const val PREVIEW_COLOR_UPDATE_INTERVAL_MS = 250L
        private const val LUMA_SAMPLE_GRID = 24
        private const val MIN_INVERTED_LUMA = 40
        private const val MAX_INVERTED_LUMA = 220
    }

    private lateinit var binding: FragmentScanBarcodeFromCameraBinding
    private val barcodeDatabase: BarcodeDatabase by inject()
    private val barcodeParser: BarcodeParser by inject()
    private val settings: Settings by inject()
    private val permissionsHelper: PermissionsHelper by inject()

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
    private var lastPreviewColorUpdateTimestamp: Long = 0L
    private var resumeScanningJob: Job? = null

    @Volatile
    @ColorInt
    private var lastAppliedIconColor: Int? = null

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
        initUi()
        setupCameraProvider()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        syncStateFromSettings()
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

    override fun onBarcodeConfirmed(barcode: Barcode) {
        pendingBarcode = null
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(
                barcode
            )

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
        lastAppliedIconColor = null
        lastPreviewColorUpdateTimestamp = 0L
        binding.seekBarZoom.valueTo = ZOOM_MAX_PROGRESS.toFloat()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
        setupTapToFocus()
    }

    private fun setupCameraProvider() {
        val currentContext = context ?: return
        val providerFuture = ProcessCameraProvider.getInstance(currentContext)
        providerFuture.addListener({
            if (!isAdded) {
                return@addListener
            }
            cameraProvider = providerFuture.get()
            if (areAllPermissionsGranted()) {
                bindCameraUseCases()
            }
        }, ContextCompat.getMainExecutor(currentContext))
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
        resumeScanningJob?.cancel()
        resumeScanningJob = null
    }

    private fun syncStateFromSettings() {
        val desiredLensFacing = if (settings.isBackCamera) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        if (lensFacing != desiredLensFacing) {
            lensFacing = desiredLensFacing
        }
        resumeScanningJob?.cancel()
        resumeScanningJob = null
        pendingBarcode = null
        isHandlingResult = false
        if (this::binding.isInitialized) {
            binding.barcodeOverlay.clear()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(scanner: BarcodeScanner, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        updateControlsColorFromPreview(imageProxy)
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

    private fun updateControlsColorFromPreview(imageProxy: ImageProxy) {
        if (!isAdded) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (now - lastPreviewColorUpdateTimestamp < PREVIEW_COLOR_UPDATE_INTERVAL_MS) {
            return
        }
        val averageLuma = calculateAverageLuma(imageProxy) ?: return
        val invertedLuma = (255 - averageLuma).coerceIn(MIN_INVERTED_LUMA, MAX_INVERTED_LUMA)
        val color = Color.rgb(invertedLuma, invertedLuma, invertedLuma)
        if (lastAppliedIconColor == color) {
            lastPreviewColorUpdateTimestamp = now
            return
        }
        lastPreviewColorUpdateTimestamp = now
        binding.root.post {
            if (!isAdded || view == null) {
                return@post
            }
            if (lastAppliedIconColor == color) {
                return@post
            }
            lastAppliedIconColor = color
            applyActionButtonTint(color)
        }
    }

    private fun calculateAverageLuma(imageProxy: ImageProxy): Int? {
        val plane = imageProxy.planes.firstOrNull() ?: return null
        val buffer = plane.buffer.duplicate().apply { rewind() }
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val width = imageProxy.width
        val height = imageProxy.height
        if (width == 0 || height == 0) {
            return null
        }
        var sum = 0L
        var count = 0
        val stepX = (width / LUMA_SAMPLE_GRID).coerceAtLeast(1)
        val stepY = (height / LUMA_SAMPLE_GRID).coerceAtLeast(1)
        val limit = buffer.limit()
        var y = 0
        while (y < height) {
            val rowOffset = y * rowStride
            var x = 0
            while (x < width) {
                val index = rowOffset + x * pixelStride
                if (index < limit) {
                    sum += buffer.get(index).toInt() and 0xFF
                    count++
                }
                x += stepX
            }
            y += stepY
        }
        if (count == 0) {
            return null
        }
        return (sum / count).toInt()
    }

    private fun applyActionButtonTint(@ColorInt color: Int) {
        val activatedColor = ColorUtils.blendARGB(color, Color.WHITE, 0.25f)
        val tint = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_activated),
                intArrayOf()
            ),
            intArrayOf(activatedColor, color),
        )
        val rippleColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 0x55))
        binding.imageViewFlash.setTextColor(tint)
        binding.imageViewFlash.iconTint = tint
        binding.imageViewFlash.rippleColor = rippleColor
        binding.imageViewScanFromFile.setTextColor(tint)
        binding.imageViewScanFromFile.iconTint = tint
        binding.imageViewScanFromFile.rippleColor = rippleColor
        binding.buttonDecreaseZoom.iconTint = tint
        binding.buttonDecreaseZoom.rippleColor = rippleColor
        binding.buttonIncreaseZoom.iconTint = tint
        binding.buttonIncreaseZoom.rippleColor = rippleColor
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
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(
                barcode
            )

            else -> {
                lastResult = barcode
                pendingBarcode = null
                navigateToBarcodeScreen(barcode)
            }
        }
    }

    private fun saveScannedBarcode(barcode: Barcode) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching { persistBarcode(barcode) }
            lastResult = barcode
            pendingBarcode = null
            result.onSuccess { id ->
                val savedBarcode = barcode.copy(id = id)
                if (settings.continuousScanning) {
                    scheduleResumeScanning(showMessage = true)
                } else {
                    navigateToBarcodeScreen(savedBarcode)
                }
            }.onFailure(::showError)
        }
    }

    private suspend fun persistBarcode(barcode: Barcode): Long = withContext(Dispatchers.IO) {
        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
    }

    private fun scheduleResumeScanning(showMessage: Boolean) {
        resumeScanningJob?.cancel()
        resumeScanningJob = resumeScanningFlow(showMessage)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { shouldShowMessage ->
                if (!isAdded) {
                    return@onEach
                }
                if (shouldShowMessage) {
                    view?.let { Snackbar.make(it, R.string.saved, Snackbar.LENGTH_LONG).show() }
                }
                binding.barcodeOverlay.clear()
                pendingBarcode = null
                isHandlingResult = false
            }
            .onCompletion { resumeScanningJob = null }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun resumeScanningFlow(showMessage: Boolean) = flow {
        delay(CONTINUOUS_SCANNING_PREVIEW_DELAY)
        emit(showMessage)
    }.flowOn(Dispatchers.Default)

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
        val clickListener = View.OnClickListener {
            ScanBarcodeFromFileActivity.start(requireActivity())
        }
        binding.layoutScanFromFileContainer.setOnClickListener(clickListener)
        binding.imageViewScanFromFile.setOnClickListener(clickListener)
    }

    private fun initFlashButton() {
        val clickListener = View.OnClickListener { toggleFlash() }
        binding.layoutFlashContainer.setOnClickListener(clickListener)
        binding.imageViewFlash.setOnClickListener(clickListener)
    }

    private fun toggleFlash() {
        val camera = camera ?: return
        val flashAvailable =
            camera.cameraInfo.hasFlashUnit() && lensFacing == CameraSelector.LENS_FACING_BACK
        if (flashAvailable.not()) {
            return
        }
        val enableTorch = binding.imageViewFlash.isActivated.not()
        camera.cameraControl.enableTorch(enableTorch)
        settings.flash = enableTorch
    }

    private fun updateFlashAvailability() {
        val camera = camera
        val flashAvailable =
            camera?.cameraInfo?.hasFlashUnit() == true && lensFacing == CameraSelector.LENS_FACING_BACK
        binding.imageViewFlash.isVisible = flashAvailable
        binding.imageViewFlash.isVisible = flashAvailable
        binding.layoutFlashContainer.isVisible = flashAvailable
        if (flashAvailable.not()) {
            settings.flash = false
        }
    }

    private fun applyInitialTorchState() {
        val camera = camera ?: return
        val flashAvailable =
            camera.cameraInfo.hasFlashUnit() && lensFacing == CameraSelector.LENS_FACING_BACK
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
            val progress =
                (zoomState.linearZoom * ZOOM_MAX_PROGRESS).toInt().coerceIn(0, ZOOM_MAX_PROGRESS)
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
            val meteringPoint =
                binding.previewView.meteringPointFactory.createPoint(event.x, event.y)
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
        if (!isAdded) {
            return
        }
        val hostActivity = activity as? AppCompatActivity ?: return
        permissionsHelper.requestNotGrantedPermissions(
            hostActivity,
            PERMISSIONS,
            PERMISSION_REQUEST_CODE,
        )
    }

    private fun areAllPermissionsGranted(): Boolean {
        if (!isAdded) {
            return false
        }
        val hostActivity = activity ?: return false
        return permissionsHelper.areAllPermissionsGranted(hostActivity, PERMISSIONS)
    }
}
