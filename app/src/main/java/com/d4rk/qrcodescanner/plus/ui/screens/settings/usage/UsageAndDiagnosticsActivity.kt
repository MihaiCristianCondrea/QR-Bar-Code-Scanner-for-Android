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
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

        binding.switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchAnalytics.isPressed) {
                viewModel.setAnalyticsConsent(isChecked)
            }
        }

        binding.switchAdStorage.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchAdStorage.isPressed) {
                viewModel.setAdStorageConsent(isChecked)
            }
        }

        binding.switchAdUserData.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchAdUserData.isPressed) {
                viewModel.setAdUserDataConsent(isChecked)
            }
        }

        binding.switchAdPersonalization.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchAdPersonalization.isPressed) {
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
            val privacyPolicyUri = getString(R.string.privacy_policy_link).toUri()
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
        updateSwitchState(binding.switchAnalytics, state.analyticsConsentGranted)
        updateSwitchState(binding.switchAdStorage, state.adStorageConsentGranted)
        updateSwitchState(binding.switchAdUserData, state.adUserDataConsentGranted)
        updateSwitchState(binding.switchAdPersonalization, state.adPersonalizationConsentGranted)

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

        binding.switchAnalytics.isEnabled = enabled
        binding.switchAdStorage.isEnabled = enabled
        binding.switchAdUserData.isEnabled = enabled
        binding.switchAdPersonalization.isEnabled = enabled

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
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(enabled)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
    }

    companion object {
        private const val KEY_ADVANCED_SETTINGS_EXPANDED = "advanced_settings_expanded"
        private const val DISABLED_ALPHA = 0.5f
        private const val ANIMATION_DURATION = 150L
    }
}
