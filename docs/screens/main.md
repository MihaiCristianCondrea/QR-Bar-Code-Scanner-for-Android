# Main

## Layers
- **Data**: Provides repositories for core application state.
- **Domain**: Houses interfaces like `MainRepository` for interacting with the app shell.
- **UI**: Navigation components such as `MainTopAppBar`.
- **Utils**: Common helpers for main application flows.

## Primary Screens
- `MainTopAppBar` – top bar with navigation and actions.

## Integration
```kotlin
MainTopAppBar(title = "App Toolkit")
```
