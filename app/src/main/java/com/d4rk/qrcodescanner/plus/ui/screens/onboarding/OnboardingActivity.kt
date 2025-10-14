package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.data.onboarding.OnboardingPreferences
import com.d4rk.qrcodescanner.plus.databinding.ActivityOnboardingBinding
import com.d4rk.qrcodescanner.plus.ui.screens.main.MainActivity
import com.d4rk.qrcodescanner.plus.utils.helpers.EdgeToEdgeHelper
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity(), OnboardingDoneFragment.Callback {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var pagerAdapter: OnboardingPagerAdapter

    private val pages = listOf(
        OnboardingPage.Theme,
        OnboardingPage.StartDestination,
        OnboardingPage.BottomNavigationLabels,
        OnboardingPage.Data,
        OnboardingPage.Done,
    )

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateNavigationForPosition(position)
            updateTabIndicators(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OnboardingPreferences.isOnboardingComplete(this)) {
            OnboardingPreferences.setFreshInstall(this, false)
            navigateToMain()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.applyEdgeToEdge(window = window, view = binding.root)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
        updateNavigationForPosition(binding.viewPager.currentItem)
        updateTabIndicators(binding.viewPager.currentItem)
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        }
        super.onDestroy()
    }

    override fun onGetStartedClicked() {
        completeOnboarding()
    }

    private fun setupViewPager() {
        pagerAdapter = OnboardingPagerAdapter(this, pages)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)

        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { tab, _ ->
            tab.customView = createIndicatorView(binding.tabIndicator)
        }.attach()
    }

    private fun setupButtons() {
        binding.buttonSkip.setOnClickListener { completeOnboarding() }
        binding.buttonBack.setOnClickListener {
            val previousPage = binding.viewPager.currentItem - 1
            if (previousPage >= 0) {
                binding.viewPager.currentItem = previousPage
            }
        }
        binding.buttonNext.setOnClickListener {
            val nextPage = binding.viewPager.currentItem + 1
            if (nextPage < pages.size) {
                binding.viewPager.currentItem = nextPage
            } else {
                completeOnboarding()
            }
        }
    }

    private fun updateNavigationForPosition(position: Int) {
        binding.buttonBack.isEnabled = position > 0
        binding.buttonBack.alpha = if (binding.buttonBack.isEnabled) 1f else 0.6f
        val isLastPage = position == pages.lastIndex
        binding.buttonNext.text = getString(
            if (isLastPage) R.string.get_started_button else R.string.next
        )
        binding.buttonSkip.isVisible = !isLastPage
    }

    private fun updateTabIndicators(selectedPosition: Int) {
        val tabLayout = binding.tabIndicator
        for (index in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(index) ?: continue
            val indicator = tab.customView ?: continue
            val layoutParams = indicator.layoutParams as? LinearLayout.LayoutParams
                ?: LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            val sizeRes = if (index == selectedPosition) {
                R.dimen.onboarding_indicator_large
            } else {
                R.dimen.onboarding_indicator_small
            }
            val size = resources.getDimensionPixelSize(sizeRes)
            layoutParams.width = size
            layoutParams.height = size
            val margin = resources.getDimensionPixelSize(R.dimen.onboarding_indicator_spacing)
            layoutParams.marginStart = margin
            layoutParams.marginEnd = margin
            indicator.layoutParams = layoutParams
            indicator.background = AppCompatResources.getDrawable(
                this,
                if (index == selectedPosition) {
                    R.drawable.onboarding_dot_selected
                } else {
                    R.drawable.onboarding_dot_unselected
                },
            )
        }
    }

    private fun createIndicatorView(tabLayout: TabLayout): View {
        val context = tabLayout.context
        val view = View(context)
        val margin = resources.getDimensionPixelSize(R.dimen.onboarding_indicator_spacing)
        val params = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.onboarding_indicator_small),
            resources.getDimensionPixelSize(R.dimen.onboarding_indicator_small),
        )
        params.marginStart = margin
        params.marginEnd = margin
        view.layoutParams = params
        view.background = AppCompatResources.getDrawable(context, R.drawable.onboarding_dot_unselected)
        return view
    }

    private fun completeOnboarding() {
        OnboardingPreferences.setOnboardingComplete(this, true)
        OnboardingPreferences.setFreshInstall(this, false)
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
