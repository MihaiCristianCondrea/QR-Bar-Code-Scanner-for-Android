# Permissions

## Layers
- **Domain**: Defines permission handling logic.
- **UI**: `PermissionsScreen` exposed via `PermissionsActivity`.

## Primary Screens
- `PermissionsScreen` – requests and explains required app permissions.

## Integration
```kotlin
startActivity(Intent(context, PermissionsActivity::class.java))
```
