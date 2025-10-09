package com.d4rk.qrcodescanner.plus.domain.settings

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.d4rk.qrcodescanner.plus.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.model.SearchEngine
import com.google.zxing.BarcodeFormat
class Settings(private val context : Context) {
    companion object {
        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
    }

    private enum class Key {
        INVERSE_BARCODE_COLORS , OPEN_LINKS_AUTOMATICALLY , COPY_TO_CLIPBOARD , SIMPLE_AUTO_FOCUS , FLASHLIGHT , VIBRATE , CONTINUOUS_SCANNING , CONFIRM_SCANS_MANUALLY , IS_BACK_CAMERA , SAVE_SCANNED_BARCODES_TO_HISTORY , SAVE_CREATED_BARCODES_TO_HISTORY , DO_NOT_SAVE_DUPLICATES , SEARCH_ENGINE ,
    }

    private val sharedPreferences by unsafeLazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME , Context.MODE_PRIVATE)
    }
    var areBarcodeColorsInversed : Boolean
        get() = get(Key.INVERSE_BARCODE_COLORS , false)
        set(value) = set(Key.INVERSE_BARCODE_COLORS , value)
    val barcodeContentColor : Int
        get() = when {
            isNightModeEnabled() && areBarcodeColorsInversed -> Color.WHITE
            else -> Color.BLACK
        }

    val barcodeBackgroundColor : Int
        get() = when {
            isNightModeEnabled() && areBarcodeColorsInversed -> Color.BLACK
            isNightModeEnabled() -> Color.WHITE
            else -> Color.TRANSPARENT
        }
    var openLinksAutomatically : Boolean
        get() = get(Key.OPEN_LINKS_AUTOMATICALLY , false)
        set(value) = set(Key.OPEN_LINKS_AUTOMATICALLY , value)
    var copyToClipboard : Boolean
        get() = get(Key.COPY_TO_CLIPBOARD , true)
        set(value) = set(Key.COPY_TO_CLIPBOARD , value)
    var simpleAutoFocus : Boolean
        get() = get(Key.SIMPLE_AUTO_FOCUS , false)
        set(value) = set(Key.SIMPLE_AUTO_FOCUS , value)
    var flash : Boolean
        get() = get(Key.FLASHLIGHT , false)
        set(value) = set(Key.FLASHLIGHT , value)
    var vibrate : Boolean
        get() = get(Key.VIBRATE , true)
        set(value) = set(Key.VIBRATE , value)
    var continuousScanning : Boolean
        get() = get(Key.CONTINUOUS_SCANNING , false)
        set(value) = set(Key.CONTINUOUS_SCANNING , value)
    var confirmScansManually : Boolean
        get() = get(Key.CONFIRM_SCANS_MANUALLY , false)
        set(value) = set(Key.CONFIRM_SCANS_MANUALLY , value)
    var isBackCamera : Boolean
        get() = get(Key.IS_BACK_CAMERA , true)
        set(value) = set(Key.IS_BACK_CAMERA , value)
    var saveScannedBarcodesToHistory : Boolean
        get() = get(Key.SAVE_SCANNED_BARCODES_TO_HISTORY , true)
        set(value) = set(Key.SAVE_SCANNED_BARCODES_TO_HISTORY , value)
    var saveCreatedBarcodesToHistory : Boolean
        get() = get(Key.SAVE_CREATED_BARCODES_TO_HISTORY , true)
        set(value) = set(Key.SAVE_CREATED_BARCODES_TO_HISTORY , value)
    var doNotSaveDuplicates : Boolean
        get() = get(Key.DO_NOT_SAVE_DUPLICATES , false)
        set(value) = set(Key.DO_NOT_SAVE_DUPLICATES , value)
    var searchEngine : SearchEngine
        get() = get(Key.SEARCH_ENGINE , SearchEngine.NONE)
        set(value) = set(Key.SEARCH_ENGINE , value)

    fun isFormatSelected(format : BarcodeFormat) : Boolean {
        return sharedPreferences.getBoolean(format.name , true)
    }

    fun setFormatSelected(format : BarcodeFormat , isSelected : Boolean) {
        sharedPreferences.edit { putBoolean(format.name , isSelected) }
    }

    private fun get(key : Key , default : Boolean = false) : Boolean {
        return sharedPreferences.getBoolean(key.name , default)
    }

    private fun set(key : Key , value : Boolean) {
        sharedPreferences.edit { putBoolean(key.name , value) }
    }

    private fun get(key : Key , default : SearchEngine = SearchEngine.NONE) : SearchEngine {
        val rawValue = sharedPreferences.getString(key.name , null) ?: default.name
        return SearchEngine.valueOf(rawValue)
    }

    private fun set(key : Key , value : SearchEngine) {
        sharedPreferences.edit { putString(key.name , value.name) }
    }

    private fun isNightModeEnabled() : Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                val nightModeMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeMask == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}
