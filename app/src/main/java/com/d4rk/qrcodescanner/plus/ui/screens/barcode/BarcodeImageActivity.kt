package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityBarcodeImageBinding
import com.d4rk.qrcodescanner.plus.domain.barcode.BarcodeImageGenerator
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.model.Barcode
import com.d4rk.qrcodescanner.plus.ui.components.navigation.UpNavigationActivity
import com.d4rk.qrcodescanner.plus.ui.components.navigation.setupToolbarWithUpNavigation
import com.d4rk.qrcodescanner.plus.utils.extension.toStringId
import com.d4rk.qrcodescanner.plus.utils.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Locale

class BarcodeImageActivity : UpNavigationActivity() {
    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"
        fun start(context: Context, barcode: Barcode) {
            val intent = Intent(context, BarcodeImageActivity::class.java)
            intent.putExtra(BARCODE_KEY, barcode)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityBarcodeImageBinding
    private val barcodeImageGenerator: BarcodeImageGenerator by inject()
    private val settings: Settings by inject()
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    @Suppress("DEPRECATION")
    private val barcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode
            ?: throw IllegalArgumentException("No barcode passed")
    }
    private var originalBrightness: Float = 0.5f
    private var optionsMenu: Menu? = null
    private var isBrightnessAtMax = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeImageBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        setupToolbarWithUpNavigation()
        saveOriginalBrightness()
        showBarcode()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_barcode_image, menu)
        optionsMenu = menu
        updateOptionsMenu()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateOptionsMenu()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_increase_brightness -> {
                increaseBrightnessToMax()
                isBrightnessAtMax = true
                updateOptionsMenu()
                true
            }

            R.id.item_decrease_brightness -> {
                restoreOriginalBrightness()
                isBrightnessAtMax = false
                updateOptionsMenu()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveOriginalBrightness() {
        originalBrightness = window.attributes.screenBrightness
    }

    private fun showBarcode() {
        showBarcodeImage()
        showBarcodeDate()
        showBarcodeFormat()
        showBarcodeText()
    }

    private fun showBarcodeImage() {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    barcodeImageGenerator.generateBitmap(
                        barcode,
                        2000,
                        2000,
                        0,
                        settings.barcodeContentColor,
                        settings.barcodeBackgroundColor
                    )
                }
            }
            result.onSuccess { bitmap ->
                if (bitmap == null) {
                    binding.imageViewBarcode.isVisible = false
                    return@onSuccess
                }
                binding.let {
                    it.imageViewBarcode.setImageBitmap(bitmap)
                    it.imageViewBarcode.setBackgroundColor(settings.barcodeBackgroundColor)
                    it.layoutBarcodeImageBackground.setBackgroundColor(settings.barcodeBackgroundColor)
                    if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO || settings.areBarcodeColorsInversed) {
                        it.layoutBarcodeImageBackground.setPadding(0, 0, 0, 0)
                    }
                }
            }.onFailure {
                binding.imageViewBarcode.isVisible = false
            }
        }
    }

    private fun showBarcodeDate() {
        binding.textViewDate.text = dateFormatter.format(barcode.date)
    }

    private fun showBarcodeFormat() {
        val format = barcode.format.toStringId()
        setTitle(format)
    }

    private fun showBarcodeText() {
        binding.textViewBarcodeText.text = barcode.text
    }

    private fun updateOptionsMenu() {
        val menu = optionsMenu ?: return
        menu.findItem(R.id.item_increase_brightness)?.isVisible = isBrightnessAtMax.not()
        menu.findItem(R.id.item_decrease_brightness)?.isVisible = isBrightnessAtMax
    }

    private fun increaseBrightnessToMax() {
        setBrightness(1.0f)
    }

    private fun restoreOriginalBrightness() {
        setBrightness(originalBrightness)
    }

    private fun setBrightness(brightness: Float) {
        window.attributes = window.attributes.apply {
            screenBrightness = brightness
        }
    }
}