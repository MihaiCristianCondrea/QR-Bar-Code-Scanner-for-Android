package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.preference.SwitchPreferenceCompat
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.di.settings

class ScannerSettingsActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.scanner

    override fun createPreferenceFragment() = ScannerSettingsFragment()

    class ScannerSettingsFragment : BasePreferenceFragment(R.xml.preferences_scanner) {
        override fun onPreferencesCreated() {
            bindSwitchPreference(
                keyResId = R.string.key_open_links_automatically,
                getter = { settings.openLinksAutomatically },
                setter = { value -> settings.openLinksAutomatically = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_copy_to_clipboard,
                getter = { settings.copyToClipboard },
                setter = { value -> settings.copyToClipboard = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_simple_auto_focus,
                getter = { settings.simpleAutoFocus },
                setter = { value -> settings.simpleAutoFocus = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_flashlight,
                getter = { settings.flash },
                setter = { value -> settings.flash = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_vibrate,
                getter = { settings.vibrate },
                setter = { value -> settings.vibrate = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_continuous_scanning,
                getter = { settings.continuousScanning },
                setter = { value -> settings.continuousScanning = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_confirm_scans_manually,
                getter = { settings.confirmScansManually },
                setter = { value -> settings.confirmScansManually = value }
            )
        }

        private fun bindSwitchPreference(
            keyResId: Int,
            getter: () -> Boolean,
            setter: (Boolean) -> Unit
        ) {
            val preference = findPreference<SwitchPreferenceCompat>(getString(keyResId))
            preference?.apply {
                isChecked = getter()
                setOnPreferenceChangeListener { _, newValue ->
                    setter(newValue as Boolean)
                    true
                }
            }
        }
    }
}
