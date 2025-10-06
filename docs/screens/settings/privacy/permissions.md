# Permissions

## Overview

The `PermissionsActivity` screen displays and manages app-specific permissions or settings that are
not covered by Android's standard runtime permission system. These are typically presented as
preferences within the app.

## Key Components

- **`PermissionsActivity.kt`**: The main activity for this screen. It hosts a
  `PreferenceFragmentCompat` to display the settings.
- **`PermissionsActivity.SettingsFragment`**: An inner class within `PermissionsActivity`. This
  fragment is responsible for loading and displaying preferences from an XML resource file (
  `R.xml.preferences_permissions`).

## UI Layout

The preferences UI is defined in `res/xml/preferences_permissions.xml`. This file contains the
different preference items that the user can interact with.

## Integration

To launch the permissions settings screen:

```kotlin
startActivity(Intent(context, PermissionsActivity::class.java))
```