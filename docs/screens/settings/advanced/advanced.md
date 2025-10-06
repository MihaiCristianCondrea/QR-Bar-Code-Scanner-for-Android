# Advanced

## Layers
- **Data**: Provides advanced configuration values.
- **Domain**: Encapsulates logic for expert options.
- **UI**: `AdvancedSettingsList` composable exposes the controls.

## Primary Screens
- `AdvancedSettingsList` – list of power‑user preferences.

## Integration
```kotlin
val snackbarHostState = remember { SnackbarHostState() }
AdvancedSettingsList(snackbarHostState = snackbarHostState)
```
