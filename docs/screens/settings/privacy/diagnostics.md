# Diagnostics

## Layers
- **UI**: Offers the `UsageAndDiagnosticsList` composable and related components.

## Primary Screens
- `UsageAndDiagnosticsList` – toggles usage and diagnostics settings.

## Integration
```kotlin
val snackbarHostState = remember { SnackbarHostState() }
UsageAndDiagnosticsList(snackbarHostState = snackbarHostState)
```
