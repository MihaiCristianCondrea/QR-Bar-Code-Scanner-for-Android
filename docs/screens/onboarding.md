# Onboarding

## Layers
- **Data**: Stores onboarding preferences and progress.
- **Domain**: Coordinates onboarding steps and events.
- **UI**: `OnboardingScreen` composable within `OnboardingActivity`.
- **Utils**: Helpers for onboarding flows.

## Primary Screens
- `OnboardingScreen` – guides the user through initial setup.

## Integration
```kotlin
startActivity(Intent(context, OnboardingActivity::class.java))
```
