package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.data.onboarding.OnboardingPreferences
import com.d4rk.qrcodescanner.plus.databinding.FragmentOnboardingDataBinding

class OnboardingDataFragment : Fragment(R.layout.fragment_onboarding_data) {

    private var _binding: FragmentOnboardingDataBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingDataBinding.bind(view)

        val context = requireContext()
        OnboardingPreferences.ensureDefaultConsents(context)
        val analyticsEnabled = OnboardingPreferences.isAnalyticsEnabled(context)
        val personalizedAds = OnboardingPreferences.isPersonalizedAdsEnabled(context)

        binding.switchCrashlytics.isChecked = analyticsEnabled
        binding.switchAds.isChecked = personalizedAds
        updateAnalyticsSummary(analyticsEnabled)
        updateAdsSummary(personalizedAds)

        binding.switchCrashlytics.setOnCheckedChangeListener { _, isChecked ->
            OnboardingPreferences.setAnalyticsEnabled(context, isChecked)
            updateAnalyticsSummary(isChecked)
        }

        binding.switchAds.setOnCheckedChangeListener { _, isChecked ->
            OnboardingPreferences.setPersonalizedAdsEnabled(context, isChecked)
            updateAdsSummary(isChecked)
        }

        binding.linkPrivacy.setOnClickListener {
            val url =
                "https://mihaicristiancondrea.github.io/profile/#privacy-policy-end-user-software"
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun updateAnalyticsSummary(enabled: Boolean) {
        binding.textDetails.setText(
            if (enabled) R.string.onboarding_data_active else R.string.onboarding_data_inactive,
        )
    }

    private fun updateAdsSummary(enabled: Boolean) {
        binding.textAdsDetails.setText(
            if (enabled) R.string.onboarding_data_ads_desc else R.string.onboarding_data_ads_disabled,
        )
    }
}
