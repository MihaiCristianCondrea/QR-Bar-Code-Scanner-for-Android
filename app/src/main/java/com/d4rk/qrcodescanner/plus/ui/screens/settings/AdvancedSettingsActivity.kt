package com.d4rk.qrcodescanner.plus.ui.screens.settings

import com.d4rk.qrcodescanner.plus.R

class AdvancedSettingsActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.advanced

    override fun createPreferenceFragment() = AdvancedSettingsFragment()

    class AdvancedSettingsFragment : BasePreferenceFragment(R.xml.preferences_advanced)
}
