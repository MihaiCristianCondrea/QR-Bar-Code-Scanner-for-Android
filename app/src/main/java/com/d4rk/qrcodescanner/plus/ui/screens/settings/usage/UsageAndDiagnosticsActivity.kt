package com.d4rk.qrcodescanner.plus.ui.screens.settings.usage

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.ActivityUsageAndDiagnosticsBinding
import com.d4rk.qrcodescanner.plus.domain.settings.UsageAndDiagnosticsPreferencesRepository
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.d4rk.qrcodescanner.plus.utils.helpers.FirebaseConsentHelper
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class UsageAndDiagnosticsActivity : BaseActivity() {

    private lateinit var binding: ActivityUsageAndDiagnosticsBinding

    private val repository: UsageAndDiagnosticsPreferencesRepository by inject()

    private val viewModel: UsageAndDiagnosticsViewModel by viewModels {
        UsageAndDiagnosticsViewModelFactory(repository)
    }

    private var advancedSettingsExpanded: Boolean = false
    private var lastAppliedAnalyticsConsent: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsageAndDiagnosticsBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window, binding.root)
        setContentView(binding.root)

        advancedSettingsExpanded =
            savedInstanceState?.getBoolean(KEY_ADVANCED_SETTINGS_EXPANDED) ?: false
        updateAdvancedSettingsVisibility(animated = false)

        setupListeners()
        observeViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_ADVANCED_SETTINGS_EXPANDED, advancedSettingsExpanded)
    }

    private fun setupListeners() {
        binding.switchUsageAndDiagnostics.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchUsageAndDiagnostics.isPressed) {
                viewModel.setUsageAndDiagnostics(isChecked)
            }
        }

        binding.rowAnalyticsConsent.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (binding.rowAnalyticsConsent.toggleSwitch.isPressed) {
                viewModel.setAnalyticsConsent(isChecked)
            }
        }

        binding.rowAdStorage.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (binding.rowAdStorage.toggleSwitch.isPressed) {
                viewModel.setAdStorageConsent(isChecked)
            }
        }

        binding.rowAdUserData.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (binding.rowAdUserData.toggleSwitch.isPressed) {
                viewModel.setAdUserDataConsent(isChecked)
            }
        }

        binding.rowAdPersonalization.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (binding.rowAdPersonalization.toggleSwitch.isPressed) {
                viewModel.setAdPersonalizationConsent(isChecked)
            }
        }

        binding.headerAdvancedSettings.setOnClickListener {
            advancedSettingsExpanded = !advancedSettingsExpanded
            updateAdvancedSettingsVisibility(animated = true)
        }

        binding.rowPersonalizedAds.setOnClickListener {
            if (binding.rowPersonalizedAds.isEnabled) {
                lifecycleScope.launch {
                    UsageConsentHelper.showConsentForm(this@UsageAndDiagnosticsActivity)
                }
            }
        }

        binding.buttonLearnMore.setOnClickListener {
            val privacyPolicyUri = getString(R.string.ads_help_center_url).toUri()
            val intent = Intent(Intent.ACTION_VIEW, privacyPolicyUri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    renderState(uiState)
                }
            }
        }
    }

    private fun renderState(state: UsageAndDiagnosticsUiState) {
        updateSwitchState(binding.switchUsageAndDiagnostics, state.usageAndDiagnosticsEnabled)
        updateSwitchState(binding.rowAnalyticsConsent.toggleSwitch, state.analyticsConsentGranted)
        updateSwitchState(binding.rowAdStorage.toggleSwitch, state.adStorageConsentGranted)
        updateSwitchState(binding.rowAdUserData.toggleSwitch, state.adUserDataConsentGranted)
        updateSwitchState(binding.rowAdPersonalization.toggleSwitch, state.adPersonalizationConsentGranted)

        applyAnalyticsCollection(state.analyticsConsentGranted)
        updateAdvancedControlsEnabled(state.usageAndDiagnosticsEnabled)
    }

    private fun updateSwitchState(switch: MaterialSwitch, isChecked: Boolean) {
        if (switch.isChecked != isChecked) {
            switch.isChecked = isChecked
        }
    }

    private fun updateAdvancedControlsEnabled(enabled: Boolean) {
        binding.containerAdvancedContent.alpha = if (enabled) 1f else DISABLED_ALPHA
        binding.containerAdvancedContent.isEnabled = enabled

        binding.rowAnalyticsConsent.setPreferenceEnabled(enabled)
        binding.rowAdStorage.setPreferenceEnabled(enabled)
        binding.rowAdUserData.setPreferenceEnabled(enabled)
        binding.rowAdPersonalization.setPreferenceEnabled(enabled)

        binding.rowPersonalizedAds.isEnabled = enabled
        binding.rowPersonalizedAds.isClickable = enabled
        binding.rowPersonalizedAds.alpha = if (enabled) 1f else DISABLED_ALPHA
        binding.iconOpenPersonalizedAds.alpha = if (enabled) 1f else DISABLED_ALPHA
        binding.iconPersonalizedAds.alpha = if (enabled) 1f else DISABLED_ALPHA
        binding.titlePersonalizedAds.alpha = if (enabled) 1f else DISABLED_ALPHA
        binding.summaryPersonalizedAds.alpha = if (enabled) 1f else DISABLED_ALPHA
    }

    private fun updateAdvancedSettingsVisibility(animated: Boolean) {
        if (animated) {
            TransitionManager.beginDelayedTransition(binding.cardAdvancedSettings)
        }
        binding.containerAdvancedContent.isVisible = advancedSettingsExpanded
        val rotation = if (advancedSettingsExpanded) 180f else 0f
        if (animated) {
            binding.iconExpandIndicator.animate()
                .rotation(rotation)
                .setDuration(ANIMATION_DURATION)
                .start()
        } else {
            binding.iconExpandIndicator.rotation = rotation
        }
    }

    private fun applyAnalyticsCollection(enabled: Boolean) {
        if (lastAppliedAnalyticsConsent == enabled) {
            return
        }
        lastAppliedAnalyticsConsent = enabled
        FirebaseConsentHelper.setAnalyticsAndCrashlyticsCollectionEnabled(this, enabled)
    }

    companion object {
        private const val KEY_ADVANCED_SETTINGS_EXPANDED = "advanced_settings_expanded"
        private const val DISABLED_ALPHA = 0.5f
        private const val ANIMATION_DURATION = 150L
    }
}
