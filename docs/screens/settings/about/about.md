# About

## Layers
- **Data**: Retrieves application details and device information for display.
- **Domain**: Defines `AboutEvent` actions and `UiAboutScreen` models.
- **UI**: Composable `AboutSettingsList` renders the about preferences.

## Primary Screens
- `AboutSettingsList` – shows app version, build info and device details.

## Integration
```kotlin
val snackbarHostState = remember { SnackbarHostState() }
AboutSettingsList(snackbarHostState = snackbarHostState)
```
