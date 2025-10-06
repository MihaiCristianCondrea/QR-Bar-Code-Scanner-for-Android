# Settings

## Layers
- **General**: Contains `data`, `domain`, and `ui` packages for common preferences.
- **Settings**: Provides `domain` and `ui` implementations for the main settings screen.
- **Utils**: Shared helpers across settings modules.

## Primary Screens
- `SettingsScreen` – entry point for all configuration sections.
- `GeneralSettingsScreen` – displays basic preferences.

## Integration
```kotlin
startActivity(Intent(context, SettingsActivity::class.java))
```
