package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.annotation.StringRes
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.domain.settings.Settings
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.RequireRestartDialog
import org.koin.android.ext.android.inject

class DisplaySettingsActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.display

    override fun createPreferenceFragment() = DisplaySettingsFragment()

    class DisplaySettingsFragment : BasePreferenceFragment(R.xml.preferences_display) {
        private val settings: Settings by inject()

        override fun onPreferencesCreated() {
            findPreference<ListPreference>(getString(R.string.key_theme))?.apply {
                bindSummary(R.string.summary_preference_settings_theme)
            }

            findPreference<ListPreference>(getString(R.string.key_bottom_navigation_bar_labels))?.apply {
                bindSummary(R.string.summary_preference_settings_bottom_navigation_bar_labels)
                setOnPreferenceChangeListener { _, _ ->
                    RequireRestartDialog().show(
                        childFragmentManager,
                        RequireRestartDialog::class.java.name
                    )
                    true
                }
            }

            findPreference<ListPreference>(getString(R.string.key_default_tab))?.apply {
                bindSummary(R.string.summary_preference_settings_default_tab)
                setOnPreferenceChangeListener { _, _ ->
                    RequireRestartDialog().show(
                        childFragmentManager,
                        RequireRestartDialog::class.java.name
                    )
                    true
                }
            }

            val invertBarcodeColors =
                findPreference<SwitchPreferenceCompat>(
                    getString(R.string.key_invert_bar_code_colors_in_dark_theme)
                )
            invertBarcodeColors?.apply {
                isChecked = settings.areBarcodeColorsInversed
                setOnPreferenceChangeListener { _, newValue ->
                    settings.areBarcodeColorsInversed = newValue as Boolean
                    true
                }
            }

            findPreference<ListPreference>(getString(R.string.key_language))?.apply {
                bindSummary(R.string.summary_preference_settings_language_value)
            }
        }

        private fun ListPreference.bindSummary(@StringRes summaryResId: Int) {
            summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
                val entry = preference.entry?.toString().orEmpty()
                val display = if (entry.isBlank()) preference.value.orEmpty() else entry
                getString(summaryResId, display)
            }
        }
    }
}
