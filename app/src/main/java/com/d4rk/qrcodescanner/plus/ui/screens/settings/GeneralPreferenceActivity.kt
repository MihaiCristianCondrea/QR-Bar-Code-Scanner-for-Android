package com.d4rk.qrcodescanner.plus.ui.screens.settings

import androidx.preference.Preference
import com.d4rk.qrcodescanner.plus.R

class GeneralPreferenceActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.settings

    override fun createPreferenceFragment() = GeneralPreferenceFragment()

    class GeneralPreferenceFragment : BasePreferenceFragment(R.xml.preferences_settings) {
        override fun onPreferencesCreated() {
            val aboutOverviewPreference =
                findPreference<Preference>(getString(R.string.key_settings_overview_about))
            aboutOverviewPreference?.summary = getString(
                R.string.summary_settings_about_overview,
                getString(R.string.app_name)
            )
        }
    }
}
