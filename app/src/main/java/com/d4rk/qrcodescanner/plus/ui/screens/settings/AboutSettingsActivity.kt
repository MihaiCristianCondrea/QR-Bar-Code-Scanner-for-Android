package com.d4rk.qrcodescanner.plus.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.preference.Preference
import com.d4rk.qrcodescanner.plus.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class AboutSettingsActivity : BasePreferenceActivity() {
    override val toolbarTitleResId: Int = R.string.about

    override fun createPreferenceFragment() = AboutSettingsFragment()

    class AboutSettingsFragment : BasePreferenceFragment(R.xml.preferences_about) {
        override fun onPreferencesCreated() {
            val deviceInfoPreference =
                findPreference<Preference>(getString(R.string.key_device_info))
            deviceInfoPreference?.let { preference ->
                val version = buildDeviceInfoSummary()
                preference.summary = version
                preference.setOnPreferenceClickListener {
                    val clipboard =
                        requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("text", version)
                    clipboard.setPrimaryClip(clip)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        Toast.makeText(
                            requireContext(),
                            R.string.snack_copied_to_clipboard,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
            }

            val ossPreference =
                findPreference<Preference>(getString(R.string.key_open_source_licenses))
            ossPreference?.summary = getString(R.string.summary_preference_settings_open_source_licenses)
            ossPreference?.setOnPreferenceClickListener {
                startActivity(Intent(requireActivity(), OssLicensesMenuActivity::class.java))
                true
            }
        }

        private fun buildDeviceInfoSummary(): String {
            return getString(
                R.string.app_build,
                "${getString(R.string.manufacturer)} ${Build.MANUFACTURER}",
                "${getString(R.string.device_model)} ${Build.MODEL}",
                "${getString(R.string.android_version)} ${Build.VERSION.RELEASE}",
                "${getString(R.string.api_level)} ${Build.VERSION.SDK_INT}",
                "${getString(R.string.arch)} ${Build.SUPPORTED_ABIS.joinToString()}"
            )
        }
    }
}
