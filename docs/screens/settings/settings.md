# Settings

## Overview

The settings experience is composed of a lightweight overview screen that links to dedicated
subscreens for each preference area. The main entry point is
`GeneralPreferenceActivity.kt`, which displays the overview list. Each destination (Display,
Scanner, History, Notifications, Security & Privacy, Advanced, and About) is implemented as its
own `*SettingsActivity`.

`SettingsViewModel.kt` still handles the business logic and data persistence for the settings and is
shared across all activities via the common `BasePreferenceActivity`.

## Key Components

- **`GeneralPreferenceActivity.kt`**: Hosts the overview list and wires up theming and locale
  changes through `BasePreferenceActivity`.
- **`BasePreferenceActivity.kt` / `BasePreferenceFragment.kt`**: Shared scaffolding used by the
  overview and every subscreen to ensure consistent styling and theming behaviour.
- **`*SettingsActivity.kt` (e.g. `DisplaySettingsActivity.kt`)**: Display the detailed preferences
  for each feature area.
- **`SettingsViewModel.kt`**: Manages settings data, listens for preference changes, and applies
  them (e.g., dark mode).

## Integration

To launch the settings screen:

```kotlin
val intent = Intent(context, GeneralPreferenceActivity::class.java)
startActivity(intent)
```
