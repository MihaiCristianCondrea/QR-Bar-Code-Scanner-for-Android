package com.d4rk.qrcodescanner.plus.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.d4rk.qrcodescanner.plus.BuildConfig
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivitySettingsBinding
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.DeleteConfirmationDialogFragment
import com.d4rk.qrcodescanner.plus.ui.components.dialogs.RequireRestartDialog
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.textview.MaterialTextView
import androidx.core.view.isNotEmpty

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, rootKey: String?) {
        val themeValues = resources.getStringArray(R.array.preference_theme_values)
        when (rootKey) {
            getString(R.string.key_theme) -> sharedPreferences?.let { pref ->
                when (pref.getString(getString(R.string.key_theme), themeValues[0])) {
                    themeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    themeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    themeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    themeValues[3] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
        val languageCode = sharedPreferences?.getString(getString(R.string.key_language), getString(R.string.default_value_language))
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var settingsList: RecyclerView? = null
        private var preferenceAdapterObserver: RecyclerView.AdapterDataObserver? = null
        private var preferenceChildAttachListener: RecyclerView.OnChildAttachStateChangeListener? = null
        private var preferenceLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_settings, rootKey)
            preferenceScreen?.let(::applyMaterialLayouts)

            val labelVisibilityMode = findPreference<ListPreference>(getString(R.string.key_bottom_navigation_bar_labels))
            labelVisibilityMode?.setOnPreferenceChangeListener { _, _ ->
                val restartDialog = RequireRestartDialog()
                restartDialog.show(childFragmentManager, RequireRestartDialog::class.java.name)
                true
            }

            val flashlightPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_flashlight))
            flashlightPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.flash = newValue as Boolean
                true
            }

            val defaultTab = findPreference<ListPreference>(getString(R.string.key_default_tab))
            defaultTab?.setOnPreferenceChangeListener { _, _ ->
                val restartDialog = RequireRestartDialog()
                restartDialog.show(childFragmentManager, RequireRestartDialog::class.java.name)
                true
            }

            val inverseBarcodeColorsInDarkThemePreference =
                findPreference<SwitchPreferenceCompat>(getString(R.string.key_invert_bar_code_colors_in_dark_theme))
            inverseBarcodeColorsInDarkThemePreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.areBarcodeColorsInversed = newValue as Boolean
                true
            }

            val doNotSaveDuplicatesPreference =
                findPreference<SwitchPreferenceCompat>(getString(R.string.key_do_not_save_duplicates))
            doNotSaveDuplicatesPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.doNotSaveDuplicates = newValue as Boolean
                true
            }

            val copyToClipboardPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_copy_to_clipboard))
            copyToClipboardPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.copyToClipboard = newValue as Boolean
                true
            }

            val simpleAutoFocusPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_simple_auto_focus))
            simpleAutoFocusPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.simpleAutoFocus = newValue as Boolean
                true
            }

            val vibratePreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_vibrate))
            vibratePreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.vibrate = newValue as Boolean
                true
            }

            val continuousScanningPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_continuous_scanning))
            continuousScanningPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.continuousScanning = newValue as Boolean
                true
            }

            val openLinksAutomaticallyPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_open_links_automatically))
            openLinksAutomaticallyPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.openLinksAutomatically = newValue as Boolean
                true
            }

            val saveScannedBarcodesPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_save_scanned_barcodes))
            saveScannedBarcodesPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.saveScannedBarcodesToHistory = newValue as Boolean
                true
            }

            val saveCreatedBarcodesPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_save_created_barcodes))
            saveCreatedBarcodesPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.saveCreatedBarcodesToHistory = newValue as Boolean
                true
            }

            val confirmScansManuallyPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.key_confirm_scans_manually))
            confirmScansManuallyPreference?.setOnPreferenceChangeListener { _, newValue ->
                settings.confirmScansManually = newValue as Boolean
                true
            }

            val changelogPreference = findPreference<Preference>(getString(R.string.key_changelog))
            changelogPreference?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(requireContext().getString(R.string.changelog_title, BuildConfig.VERSION_NAME))
                    .setIcon(R.drawable.ic_changelog)
                    .setMessage(R.string.changes)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }

            val notificationsSettings = findPreference<Preference>(getString(R.string.key_notifications_settings))
            notificationsSettings?.setOnPreferenceClickListener {
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

            val sharePreference = findPreference<Preference>(getString(R.string.key_share))
            sharePreference?.setOnPreferenceClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    putExtra(Intent.EXTRA_SUBJECT, R.string.share_subject)
                }
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
                true
            }

            val ossPreference = findPreference<Preference>(getString(R.string.key_open_source_licenses))
            ossPreference?.setOnPreferenceClickListener {
                startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
                true
            }

            val deviceInfoPreference = findPreference<Preference>(getString(R.string.key_device_info))
            deviceInfoPreference?.let { preference ->
                val version = buildDeviceInfoSummary()
                preference.summary = version
                preference.setOnPreferenceClickListener {
                    val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("text", version)
                    clipboard.setPrimaryClip(clip)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        Toast.makeText(requireContext(), R.string.snack_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }

            val cleanHistoryPreference = findPreference<Preference>(getString(R.string.key_clean_history))
            cleanHistoryPreference?.setOnPreferenceClickListener {
                val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.summary_delete_history)
                dialog.show(childFragmentManager, "")
                true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setDivider(null)
            setDividerHeight(0)
            val listView = listView
            settingsList = listView
            val verticalPadding = resources.getDimensionPixelSize(R.dimen.preference_list_vertical_padding)
            listView.setPadding(listView.paddingLeft, verticalPadding, listView.paddingRight, verticalPadding)
            listView.clipToPadding = false
            setupPreferenceCardStyling(listView)
        }

        override fun onDestroyView() {
            settingsList?.let { recyclerView ->
                recyclerView.adapter?.let { adapter ->
                    preferenceAdapterObserver?.let(adapter::unregisterAdapterDataObserver)
                }
                preferenceChildAttachListener?.let(recyclerView::removeOnChildAttachStateChangeListener)
                preferenceLayoutListener?.let { listener ->
                    val observer = recyclerView.viewTreeObserver
                    if (observer.isAlive) {
                        observer.removeOnGlobalLayoutListener(listener)
                    }
                }
            }
            preferenceAdapterObserver = null
            preferenceChildAttachListener = null
            preferenceLayoutListener = null
            settingsList = null
            super.onDestroyView()
        }

        private fun applyMaterialLayouts(group: PreferenceGroup) {
            for (index in 0 until group.preferenceCount) {
                val preference = group.getPreference(index)
                when (preference) {
                    is PreferenceCategory -> {
                        preference.layoutResource = R.layout.item_preference_category
                    }
                    is SwitchPreferenceCompat -> {
                        preference.layoutResource = R.layout.item_preference
                        preference.widgetLayoutResource = R.layout.widget_preference_switch
                    }
                    else -> {
                        preference.layoutResource = R.layout.item_preference
                    }
                }
                preference.isIconSpaceReserved = false
                if (preference is PreferenceGroup) {
                    applyMaterialLayouts(preference)
                }
            }
        }

        private fun setupPreferenceCardStyling(listView: RecyclerView) {
            val updateRunnable = Runnable { updatePreferenceCardShapes(listView) }
            listView.adapter?.let { adapter ->
                val observer = object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        updateRunnable.run()
                    }

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        updateRunnable.run()
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        updateRunnable.run()
                    }

                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        updateRunnable.run()
                    }

                    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                        updateRunnable.run()
                    }
                }
                adapter.registerAdapterDataObserver(observer)
                preferenceAdapterObserver = observer
            }
            val attachListener = object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    updateRunnable.run()
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    updateRunnable.run()
                }
            }
            listView.addOnChildAttachStateChangeListener(attachListener)
            preferenceChildAttachListener = attachListener
            val layoutListener = ViewTreeObserver.OnGlobalLayoutListener { updateRunnable.run() }
            listView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
            preferenceLayoutListener = layoutListener
            listView.post(updateRunnable)
        }

        private fun updatePreferenceCardShapes(listView: RecyclerView) {
            val adapter = listView.adapter ?: return
            val screen = preferenceScreen ?: return
            val preferences = getVisiblePreferences(screen)
            val itemCount = minOf(adapter.itemCount, preferences.size)
            val spacing = resources.getDimensionPixelSize(R.dimen.preference_item_spacing)
            for (position in 0 until itemCount) {
                val preference = preferences[position]
                val holder = listView.findViewHolderForAdapterPosition(position) ?: continue
                val itemView = holder.itemView
                if (preference is PreferenceCategory) {
                    val titleView = itemView.findViewById<MaterialTextView>(android.R.id.title)
                    titleView?.setCompoundDrawablesRelativeWithIntrinsicBounds(preference.icon, null, null, null)
                    (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                        val topMargin = if (position == 0) 0 else spacing
                        val bottomMargin = spacing
                        var updated = false
                        if (params.topMargin != topMargin) {
                            params.topMargin = topMargin
                            updated = true
                        }
                        if (params.bottomMargin != bottomMargin) {
                            params.bottomMargin = bottomMargin
                            updated = true
                        }
                        if (updated) {
                            itemView.layoutParams = params
                        }
                    }
                    continue
                }
                val card = when (itemView) {
                    is MaterialCardView -> itemView
                    else -> itemView.findViewById(R.id.lesson_card)
                }
                val first = isFirstPreferenceInSection(preferences, position)
                val last = isLastPreferenceInSection(preferences, position)
                applyRoundedCorners(card, first, last)
                (card.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                    var updated = false
                    if (params.topMargin != 0) {
                        params.topMargin = 0
                        updated = true
                    }
                    val bottomMargin = if (last) 0 else spacing
                    if (params.bottomMargin != bottomMargin) {
                        params.bottomMargin = bottomMargin
                        updated = true
                    }
                    if (updated) {
                        card.layoutParams = params
                    }
                }
                syncAccessoryVisibility(card)
            }
        }

        private fun isFirstPreferenceInSection(preferences: List<Preference>, position: Int): Boolean {
            for (index in position - 1 downTo 0) {
                val previous = preferences[index]
                if (!previous.isVisible) continue
                return previous is PreferenceCategory
            }
            return true
        }

        private fun isLastPreferenceInSection(preferences: List<Preference>, position: Int): Boolean {
            for (index in position + 1 until preferences.size) {
                val next = preferences[index]
                if (!next.isVisible) continue
                return next is PreferenceCategory
            }
            return true
        }

        private fun applyRoundedCorners(card: MaterialCardView, first: Boolean, last: Boolean) {
            val metrics = card.resources.displayMetrics
            val smallRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, metrics)
            val largeRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, metrics)
            val shape = card.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, if (first) largeRadius else smallRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, if (first) largeRadius else smallRadius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, if (last) largeRadius else smallRadius)
                .setBottomRightCorner(CornerFamily.ROUNDED, if (last) largeRadius else smallRadius)
                .build()
            card.shapeAppearanceModel = shape
        }

        private fun syncAccessoryVisibility(itemView: View) {
            val iconView = itemView.findViewById<ImageView>(android.R.id.icon)
            iconView?.let { icon ->
                val hasIcon = icon.drawable != null
                icon.visibility = if (hasIcon) View.VISIBLE else View.GONE
            }
            val widgetFrame = itemView.findViewById<ViewGroup>(android.R.id.widget_frame)
            widgetFrame?.let { frame ->
                val hasChild = frame.isNotEmpty()
                frame.visibility = if (hasChild) View.VISIBLE else View.GONE
                if (hasChild) {
                    for (index in 0 until frame.childCount) {
                        frame.getChildAt(index).isDuplicateParentStateEnabled = true
                    }
                }
            }
        }

        private fun getVisiblePreferences(group: PreferenceGroup): List<Preference> {
            val result = mutableListOf<Preference>()
            collectVisiblePreferences(group, result)
            return result
        }

        private fun collectVisiblePreferences(group: PreferenceGroup, out: MutableList<Preference>) {
            for (index in 0 until group.preferenceCount) {
                val preference = group.getPreference(index)
                if (!preference.isVisible) continue
                out.add(preference)
                if (preference is PreferenceGroup && preference !is PreferenceScreen) {
                    collectVisiblePreferences(preference, out)
                }
            }
        }

        private fun buildDeviceInfoSummary(): String {
            return String.format(
                resources.getString(R.string.app_build),
                "${resources.getString(R.string.manufacturer)} ${Build.MANUFACTURER}",
                "${resources.getString(R.string.device_model)} ${Build.MODEL}",
                "${resources.getString(R.string.android_version)} ${Build.VERSION.RELEASE}",
                "${resources.getString(R.string.api_level)} ${Build.VERSION.SDK_INT}",
                "${resources.getString(R.string.arch)} ${Build.SUPPORTED_ABIS.joinToString()}"
            )
        }
    }
}
