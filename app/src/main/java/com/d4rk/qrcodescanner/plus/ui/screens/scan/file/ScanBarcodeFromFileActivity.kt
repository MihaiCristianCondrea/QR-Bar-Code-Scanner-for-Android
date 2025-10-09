package com.d4rk.qrcodescanner.plus.ui.screens.scan.file

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityScanBarcodeFromFileBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeImageScanner
import com.d4rk.qrcodescanner.plus.di.barcodeParser
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.d4rk.qrcodescanner.plus.utils.extension.orZero
import com.d4rk.qrcodescanner.plus.utils.extension.showError
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.zxing.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.mlkit.vision.barcode.common.Barcode as MlKitBarcode

class ScanBarcodeFromFileActivity : BaseActivity() {
    private lateinit var binding : ActivityScanBarcodeFromFileBinding
    private val photoPickerLauncher : ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            handlePickedImage(uri)
        }

    private val legacyImagePickerLauncher : ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handlePickedImage(uri)
        }

    companion object {
        private const val STATE_CURRENT_IMAGE_URI = "state_current_image_uri"
        fun start(context : Context) {
            val intent = Intent(context , ScanBarcodeFromFileActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var lastScanResult : MlKitBarcode? = null
    private var scanJob : Job? = null
    private var currentImageUri : Uri? = null
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBarcodeFromFileBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        binding.buttonScan.isEnabled = false
        val restored = savedInstanceState?.getString(STATE_CURRENT_IMAGE_URI)?.let {
            handlePickedImage(it.toUri())
            true
        } ?: false
        if (!restored && !handleIncomingIntent(intent)) {
            selectImage()
        }
        handleImageCropAreaChanged()
        handleScanButtonClicked()
    }

    override fun onSaveInstanceState(outState : Bundle) {
        super.onSaveInstanceState(outState)
        currentImageUri?.let { uri ->
            outState.putString(STATE_CURRENT_IMAGE_URI , uri.toString())
        }
    }

    override fun onNewIntent(intent : Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (!handleIncomingIntent(intent)) {
            selectImage()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
    }

    private fun selectImage() {
        if (isPhotoPickerAvailable()) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        else {
            legacyImagePickerLauncher.launch("image/*")
        }
    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        menuInflater.inflate(R.menu.menu_scan_barcode_from_image , menu)
        return true
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.item_rotate_left -> {
                // binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D)
                true
            }

            R.id.item_rotate_right -> {
                //  binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
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
            val uri = currentImageUri
            if (uri != null) {
                scanSelectedImage(uri)
            }
        }
    }

    private fun scanSelectedImage(uri : Uri) {
        scanJob?.cancel()
        binding.buttonScan.isEnabled = false
        scanJob = lifecycleScope.launch {
            try {
                val bitmap = loadBitmapFromUri(uri)
                val result = withContext(Dispatchers.Default) { barcodeImageScanner.parse(bitmap) }
                lastScanResult = result
                saveScanResult()
            } catch (_ : NotFoundException) {
                lastScanResult = null
            } catch (_ : SecurityException) {
                lastScanResult = null
            } catch (error : Exception) {
                showError(error)
            } finally {
                if (lastScanResult == null) {
                    binding.buttonScan.isEnabled = true
                }
            }
        }
    }

    private suspend fun loadBitmapFromUri(uri : Uri) : Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver , uri)
                    ImageDecoder.decodeBitmap(source)
                }
                else {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                            ?: throw IllegalStateException("Unable to decode image")
                    } ?: throw IllegalStateException("Unable to decode image")
                }
            } catch (securityException : SecurityException) {
                throw securityException
            }
        }
    }

    private fun handlePickedImage(uri : Uri?) {
        if (uri == null) {
            finish()
            return
        }
        currentImageUri = uri
        lastScanResult = null
        binding.buttonScan.isEnabled = true
        binding.imageViewPreview.setImageURI(uri)
    }

    private fun handleIncomingIntent(intent : Intent?) : Boolean {
        val uri = intent?.extractImageUri() ?: return false
        handlePickedImage(uri)
        return true
    }

    private fun Intent.extractImageUri() : Uri? {
        if (action == Intent.ACTION_SEND) {
            val streamUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(Intent.EXTRA_STREAM , Uri::class.java)
            }
            else {
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

    private fun isPhotoPickerAvailable() : Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2
            else -> false
        }
    }

    private fun saveScanResult() {
        val barcode = lastScanResult?.let(barcodeParser::parse) ?: return
        if (settings.saveScannedBarcodesToHistory.not()) {
            navigateToBarcodeScreen(barcode)
            return
        }
        lifecycleScope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    barcodeDatabase.save(barcode , settings.doNotSaveDuplicates)
                }
                navigateToBarcodeScreen(barcode.copy(id = id))
            } catch (error : Exception) {
                showError(error)
            }
        }
    }

    private fun navigateToBarcodeScreen(barcode : Barcode) {
        BarcodeActivity.start(this , barcode)
        finish()
    }
}