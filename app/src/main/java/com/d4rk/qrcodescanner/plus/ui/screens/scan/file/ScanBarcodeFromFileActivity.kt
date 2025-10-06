package com.d4rk.qrcodescanner.plus.ui.screens.scan.file

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityScanBarcodeFromFileBinding
import com.d4rk.qrcodescanner.plus.di.barcodeDatabase
import com.d4rk.qrcodescanner.plus.di.barcodeImageScanner
import com.d4rk.qrcodescanner.plus.di.barcodeParser
import com.d4rk.qrcodescanner.plus.di.permissionsHelper
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.domain.history.save
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.extension.showError
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanBarcodeFromFileActivity : BaseActivity() {
    private lateinit var binding : ActivityScanBarcodeFromFileBinding
    private val pickMediaLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK) {
            Snackbar.make(binding.root , "Failed to retrieve media." , Snackbar.LENGTH_SHORT).show()
        }
        else {
            val uri = it.data?.data
            if (uri != null) {/* binding.cropImageView.load(uri)
                        .execute(object : LoadCallback {
                            override fun onSuccess() {
                                scanCroppedImage()
                                binding.cropImageView.invalidate()
                            }
                            override fun onError(e: Throwable) {
                                showErrorOrRequestPermissions(e)
                            }
                        })*/
            }
        }
    }

    companion object {
        private const val CHOOSE_FILE_REQUEST_CODE = 12
        private const val PERMISSIONS_REQUEST_CODE = 14
        private val PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        fun start(context : Context) {
            val intent = Intent(context , ScanBarcodeFromFileActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var lastScanResult : Result? = null
    private var scanJob : Job? = null
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBarcodeFromFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectImage()
        supportEdgeToEdge()
        handleImageCropAreaChanged()
        handleScanButtonClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true , applyBottom = true)
    }

    private fun showErrorOrRequestPermissions(error : Throwable) {
        when (error) {
            is SecurityException -> permissionsHelper.requestPermissions(this , PERMISSIONS , PERMISSIONS_REQUEST_CODE)
            else -> showError(error)
        }
    }

    private fun selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            pickMediaLauncher.launch(
                Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                            type = "image/*"
                        })
        }
        else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            @Suppress("DEPRECATION") startActivityForResult(intent , CHOOSE_FILE_REQUEST_CODE)
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
            if (lastScanResult != null) {
                saveScanResult()
            }
        }
    }

    private fun scanCroppedImage() {
        scanJob?.cancel()
        lastScanResult = null
        //scanCroppedImage(binding.cropImageView.croppedBitmap)
    }

    private fun scanCroppedImage(image : Bitmap) {
        scanJob = lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.Default) { barcodeImageScanner.parse(image) }
                lastScanResult = result
            } catch (error : Exception) {
                showError(error)
            }
        }
    }

    private fun saveScanResult() {
        val barcode = lastScanResult?.let(barcodeParser::parseResult) ?: return
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