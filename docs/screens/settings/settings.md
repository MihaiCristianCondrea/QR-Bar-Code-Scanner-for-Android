# Settings

## Overview

The settings screen allows users to configure application preferences. The main entry point is
`SettingsActivity.kt`, which hosts `SettingsFragment.kt` for displaying the preference items. A
`SettingsViewModel.kt` handles the business logic and data persistence for the settings.

## Key Components

- **`SettingsActivity.kt`**: The main activity for settings. It sets up the fragment and handles
  theme changes based on preferences.
- **`SettingsFragment.kt`**: Displays the actual settings UI using AndroidX Preference components.
- **`SettingsViewModel.kt`**: Manages settings data, listens for preference changes, and applies
  them (e.g., dark mode).

## Integration

To launch the settings screen:

```kotlin
val intent = Intent(context, SettingsActivity::class.java)
startActivity(intent)
```