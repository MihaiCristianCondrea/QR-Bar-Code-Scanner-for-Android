package com.d4rk.qrcodescanner.plus.ui.screens.scan.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.ads.AdUtils
import com.d4rk.qrcodescanner.plus.databinding.ActivityScanBarcodeFromFileBinding
import com.d4rk.qrcodescanner.plus.domain.history.BarcodeDatabase
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeImageScanner
import com.d4rk.qrcodescanner.plus.domain.scan.BarcodeParser
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ScanBarcodeFromFileActivity : BaseActivity() {
    private lateinit var binding: ActivityScanBarcodeFromFileBinding
    private val barcodeImageScanner: BarcodeImageScanner by inject()
    private val barcodeParser: BarcodeParser by inject()
    private val barcodeDatabase: BarcodeDatabase by inject()
    private val settings: Settings by inject()
    private val photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            handlePickedImage(uri)
        }

    private val legacyImagePickerLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handlePickedImage(uri)
        }

    private val viewModel: ScanBarcodeFromFileViewModel by viewModels {
        ScanBarcodeFromFileViewModelFactory(
            application = application,
            barcodeImageScanner = barcodeImageScanner,
            barcodeParser = barcodeParser,
            barcodeDatabase = barcodeDatabase,
            settings = settings
        )
    }

    companion object {
        private const val STATE_CURRENT_IMAGE_URI = "state_current_image_uri"
        private const val STATE_CURRENT_ROTATION = "state_current_rotation"
        fun start(context: Context) {
            val intent = Intent(context, ScanBarcodeFromFileActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var displayedImageUri: Uri? = null
    private var currentRotationDegrees: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBarcodeFromFileBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        binding.buttonScan.isEnabled = false
        AdUtils.loadBanner(binding.nativeAdView)
        currentRotationDegrees = savedInstanceState?.getFloat(STATE_CURRENT_ROTATION) ?: 0f
        applyCurrentRotation()
        observeViewModel()
        val restored = savedInstanceState?.getString(STATE_CURRENT_IMAGE_URI)?.let {
            handlePickedImage(it.toUri(), resetRotation = false)
            true
        } ?: false
        if (!restored && !handleIncomingIntent(intent)) {
            selectImage()
        }
        handleImageCropAreaChanged()
        handleScanButtonClicked()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.uiState.value.selectedImageUri?.let { uri ->
            outState.putString(STATE_CURRENT_IMAGE_URI, uri.toString())
        }
        outState.putFloat(STATE_CURRENT_ROTATION, currentRotationDegrees)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (!handleIncomingIntent(intent)) {
            selectImage()
        }
    }

    private fun selectImage() {
        if (isPhotoPickerAvailable()) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            legacyImagePickerLauncher.launch("image/*")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scan_barcode_from_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_rotate_left -> {
                rotateImageBy(-90f)
                true
            }

            R.id.item_rotate_right -> {
                rotateImageBy(90f)
                true
            }

            R.id.item_change_image -> {
                selectImage()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleImageCropAreaChanged() {
        // TODO: implement if needed using coroutines or Flow
    }

    private fun handleScanButtonClicked() {
        binding.buttonScan.setOnClickListener {
            viewModel.scanSelectedImage()
        }
    }

    private fun handlePickedImage(uri: Uri?, resetRotation: Boolean = true) {
        if (uri == null) {
            finish()
            return
        }
        if (resetRotation) {
            resetRotation()
        }
        viewModel.onImagePicked(uri)
    }

    private fun handleIncomingIntent(intent: Intent?): Boolean {
        val uri = intent?.extractImageUri() ?: return false
        handlePickedImage(uri)
        return true
    }

    private fun Intent.extractImageUri(): Uri? {
        if (action == Intent.ACTION_SEND) {
            val streamUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (streamUri != null) return streamUri
        }
        data?.let { return it }
        if (clipData != null && clipData?.itemCount.orZero() > 0) {
            return clipData?.getItemAt(0)?.uri
        }
        return null
    }

    private fun isPhotoPickerAvailable(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2

            else -> false
        }
    }

    private fun navigateToBarcodeScreen(barcode: Barcode) {
        BarcodeActivity.start(this, barcode)
        finish()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        binding.buttonScan.isEnabled = uiState.isScanButtonEnabled
                        val uri = uiState.selectedImageUri
                        if (uri != displayedImageUri) {
                            displayedImageUri = uri
                            if (uri != null) {
                                binding.imageViewPreview.setImageURI(uri)
                                applyCurrentRotation()
                            } else {
                                binding.imageViewPreview.setImageDrawable(null)
                            }
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is ScanBarcodeFromFileEvent.NavigateToBarcode -> navigateToBarcodeScreen(
                                event.barcode
                            )

                            is ScanBarcodeFromFileEvent.ShowError -> showError(event.throwable)
                            ScanBarcodeFromFileEvent.ShowNoBarcodeFound -> showNoBarcodeFoundMessage()
                        }
                    }
                }
            }
        }
    }

    private fun showNoBarcodeFoundMessage() {
        Toast.makeText(
            this,
            R.string.scan_barcode_from_image_not_found,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun rotateImageBy(deltaDegrees: Float) {
        if (displayedImageUri == null) return
        currentRotationDegrees = normalizeRotation(currentRotationDegrees + deltaDegrees)
        applyCurrentRotation()
    }

    private fun resetRotation() {
        currentRotationDegrees = 0f
        applyCurrentRotation()
    }

    private fun applyCurrentRotation() {
        binding.imageViewPreview.rotation = currentRotationDegrees
    }

    private fun normalizeRotation(rotation: Float): Float {
        var normalized = rotation % 360f
        if (normalized < 0f) {
            normalized += 360f
        }
        return normalized
    }
}
