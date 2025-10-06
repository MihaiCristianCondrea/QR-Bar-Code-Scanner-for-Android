# Onboarding

## Overview

The `OnboardingActivity` guides users through the initial setup and configuration of the
application. It utilizes a `ViewPager2` to present a series of steps as fragments. User preferences
selected during onboarding are saved via `OnboardingViewModel`.

## Key Components

- **`OnboardingActivity.kt`**: The main activity orchestrating the onboarding flow.
- **`ActivityOnboardingBinding`**: View binding class for `activity_onboarding.xml`.
- **`OnboardingViewModel.kt`**: ViewModel responsible for managing onboarding state, such as the
  current page and marking onboarding as complete.
- **`OnboardingPagerAdapter`**: A `FragmentStateAdapter` that provides the fragments for each step
  of the onboarding process.
- **Fragments**:
    - `ThemeFragment`: Allows the user to select app theme preferences.
    - `StartPageFragment`: Allows the user to configure their preferred start page.
    - `FontFragment`: Allows the user to customize font settings.
    - `BottomLabelsFragment`: Allows the user to configure bottom navigation label visibility.
    - `DataFragment`: Handles data-related preferences or information.
    - `DoneFragment`: The final screen indicating the completion of the onboarding process.
- **Layout**: `activity_onboarding.xml` (implicitly, based on `ActivityOnboardingBinding`) which
  includes:
    - A `ViewPager2` for swiping through onboarding steps.
    - A `TabLayout` (likely styled as dot indicators) to show progress.
    - Navigation buttons: "Back", "Skip", and "Next".

## Flow

1. The activity initializes the `ViewPager2` with `OnboardingPagerAdapter`.
2. It checks for and requests UMP consent if required.
3. Users navigate through the fragments using swipe gestures or the "Next" and "Back" buttons.
4. The "Skip" button allows users to bypass the onboarding process.
5. Selections made in each fragment (e.g., Theme, Start Page) are saved, often when navigating away
   from the fragment or by explicitly pressing "Next".
6. The `OnboardingViewModel` tracks the current page and saves the onboarding completion status.
7. Upon completion (either by finishing the last step or skipping), the user is navigated to
   `MainActivity`.

## Integration

To start the onboarding flow:

```kotlin
val intent = Intent(context, OnboardingActivity::class.java)
startActivity(intent)
```