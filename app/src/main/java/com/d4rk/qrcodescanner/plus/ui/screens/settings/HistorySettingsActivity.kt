package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.DeleteConfirmationDialogFragment

class HistorySettingsActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.history

    override fun createPreferenceFragment() = HistorySettingsFragment()

    class HistorySettingsFragment : BasePreferenceFragment(R.xml.preferences_history) {
        override fun onPreferencesCreated() {
            bindSwitchPreference(
                keyResId = R.string.key_save_scanned_barcodes,
                getter = { settings.saveScannedBarcodesToHistory },
                setter = { value -> settings.saveScannedBarcodesToHistory = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_save_created_barcodes,
                getter = { settings.saveCreatedBarcodesToHistory },
                setter = { value -> settings.saveCreatedBarcodesToHistory = value }
            )
            bindSwitchPreference(
                keyResId = R.string.key_do_not_save_duplicates,
                getter = { settings.doNotSaveDuplicates },
                setter = { value -> settings.doNotSaveDuplicates = value }
            )

            val clearHistory = findPreference<Preference>(getString(R.string.key_clean_history))
            clearHistory?.setOnPreferenceClickListener {
                DeleteConfirmationDialogFragment.newInstance(R.string.summary_delete_history)
                    .show(childFragmentManager, DeleteConfirmationDialogFragment::class.java.name)
                true
            }
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
