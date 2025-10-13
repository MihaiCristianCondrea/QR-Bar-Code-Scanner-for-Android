package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.FragmentOnboardingDoneBinding

class OnboardingDoneFragment : Fragment(R.layout.fragment_onboarding_done) {

    interface Callback {
        fun onGetStartedClicked()
    }

    private var _binding: FragmentOnboardingDoneBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingDoneBinding.bind(view)

        binding.buttonGetStarted.setOnClickListener {
            (activity as? Callback)?.onGetStartedClicked()
                ?: (activity as? OnboardingActivity)?.onGetStartedClicked()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
