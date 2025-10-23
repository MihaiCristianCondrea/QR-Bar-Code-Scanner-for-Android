package com.d4rk.qrcodescanner.plus.ui.screens.barcode

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
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

private const val EXTRA_BARCODE = "com.d4rk.qrcodescanner.plus.extra.BARCODE"

class BarcodeImageActivity : UpNavigationActivity() {
    data class Args(val barcode: Barcode) {
        /**
         * Contract for launching [BarcodeImageActivity].
         *
         * @property barcode the barcode that should be rendered on the screen. This value is required
         * and callers should obtain it from the history database, scanner result, or creation flow
         * before starting the Activity.
         */
        fun toIntent(context: Context): Intent = Intent(context, BarcodeImageActivity::class.java).apply {
            putExtras(toBundle())
        }

        fun toBundle() = bundleOf(EXTRA_BARCODE to barcode)

        companion object {
            fun fromIntent(intent: Intent?): Args? {
                val barcode = intent?.readBarcodeExtra() ?: return null
                return Args(barcode)
            }
        }
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context, args: Args): Intent = args.toIntent(context)

        @JvmStatic
        fun start(context: Context, args: Args) {
            context.startActivity(createIntent(context, args))
        }

        @JvmStatic
        fun start(context: Context, barcode: Barcode) {
            start(context, Args(barcode))
        }
    }

    private lateinit var binding: ActivityBarcodeImageBinding
    private val barcodeImageGenerator: BarcodeImageGenerator by inject()
    private val settings: Settings by inject()
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    private val args: Args? by unsafeLazy { Args.fromIntent(intent) }
    private var shouldDisplayBarcodeContent = false
    private var originalBrightness: Float = 0.5f
    private var optionsMenu: Menu? = null
    private var isBrightnessAtMax = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeImageBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)
        setupToolbarWithUpNavigation()

        val barcode = args?.barcode ?: run {
            showMissingBarcodeState()
            return
        }

        showBarcodeContent()

        saveOriginalBrightness()
        showBarcode(barcode)
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!hasBarcodeContent()) {
            return false
        }
        menuInflater.inflate(R.menu.menu_barcode_image, menu)
        optionsMenu = menu
        updateOptionsMenu()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (!hasBarcodeContent()) {
            return super.onPrepareOptionsMenu(menu)
        }
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

    private fun showBarcode(barcode: Barcode) {
        showBarcodeImage(barcode)
        showBarcodeDate(barcode)
        showBarcodeFormat(barcode)
        showBarcodeText(barcode)
    }

    private fun showBarcodeImage(barcode: Barcode) {
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

    private fun showBarcodeDate(barcode: Barcode) {
        binding.textViewDate.text = dateFormatter.format(barcode.date)
    }

    private fun showBarcodeFormat(barcode: Barcode) {
        val format = barcode.format.toStringId()
        setTitle(format)
    }

    private fun showBarcodeText(barcode: Barcode) {
        binding.textViewBarcodeText.text = barcode.text
    }

    private fun updateOptionsMenu() {
        val menu = optionsMenu ?: return
        menu.findItem(R.id.item_increase_brightness)?.isVisible = isBrightnessAtMax.not()
        menu.findItem(R.id.item_decrease_brightness)?.isVisible = isBrightnessAtMax
    }

    private fun hasBarcodeContent(): Boolean = shouldDisplayBarcodeContent

    private fun showBarcodeContent() {
        shouldDisplayBarcodeContent = true
        binding.layoutMissingBarcodeState.isVisible = false
        binding.scrollView.isVisible = true
        invalidateOptionsMenu()
    }

    private fun showMissingBarcodeState() {
        shouldDisplayBarcodeContent = false
        binding.scrollView.isVisible = false
        binding.layoutMissingBarcodeState.isVisible = true
        binding.buttonMissingBarcodeAction.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setTitle(R.string.barcode_error_missing_extra)
        invalidateOptionsMenu()
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

@Suppress("DEPRECATION")
private fun Intent.readBarcodeExtra(): Barcode? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(EXTRA_BARCODE, Barcode::class.java)
    } else {
        getSerializableExtra(EXTRA_BARCODE) as? Barcode
    }
}

