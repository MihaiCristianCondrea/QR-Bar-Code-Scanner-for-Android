package com.d4rk.qrcodescanner.plus.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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

            val notificationSettings =
                findPreference<Preference>(getString(R.string.key_notifications_settings))
            notificationSettings?.setOnPreferenceClickListener {
                val context = context ?: return@setOnPreferenceClickListener false
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                }
                startActivity(intent)
                true
            }
        }
    }
}
