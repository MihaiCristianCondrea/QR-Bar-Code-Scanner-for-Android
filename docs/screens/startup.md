# Startup

## Layers
- **Domain**: Handles tasks that must run before the app launches.
- **UI**: `StartupScreen` shown by `StartupActivity`.
- **Utils**: Helpers for initiating startup actions.

## Primary Screens
- `StartupScreen` – splash and initial loading screen.

## Integration
```kotlin
startActivity(Intent(context, StartupActivity::class.java))
```
