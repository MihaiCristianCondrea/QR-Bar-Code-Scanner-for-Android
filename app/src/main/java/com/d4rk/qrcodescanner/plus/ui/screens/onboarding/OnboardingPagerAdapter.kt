package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter(
    activity: FragmentActivity,
    private val pages: List<OnboardingPage>,
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return when (pages[position]) {
            OnboardingPage.Theme -> OnboardingSelectionFragment.newInstance(
                OnboardingSelectionFragment.SelectionType.THEME,
            )
            OnboardingPage.StartDestination -> OnboardingSelectionFragment.newInstance(
                OnboardingSelectionFragment.SelectionType.START_DESTINATION,
            )
            OnboardingPage.BottomNavigationLabels -> OnboardingBottomLabelsFragment()
            OnboardingPage.Data -> OnboardingDataFragment()
            OnboardingPage.Done -> OnboardingDoneFragment()
        }
    }
}

enum class OnboardingPage {
    Theme,
    StartDestination,
    BottomNavigationLabels,
    Data,
    Done,
}
