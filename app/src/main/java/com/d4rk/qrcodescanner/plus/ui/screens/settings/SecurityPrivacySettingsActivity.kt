package com.d4rk.qrcodescanner.plus.ui.screens.settings

import com.d4rk.qrcodescanner.plus.R

class SecurityPrivacySettingsActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.security_and_privacy

    override fun createPreferenceFragment() =
        SecurityPrivacySettingsFragment()

    class SecurityPrivacySettingsFragment :
        BasePreferenceFragment(R.xml.preferences_security_privacy)
}
