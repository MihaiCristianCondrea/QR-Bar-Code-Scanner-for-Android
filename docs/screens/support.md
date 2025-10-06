# Support

## Layers
- **Domain**: Defines interactions for contacting support and managing purchases.
- **UI**: `SupportScreen` composable hosted by `SupportActivity`.
- **Billing**: Integrates in‑app billing helpers.
- **Utils**: Shared support utilities.

## Primary Screens
- `SupportScreen` – provides links to contact or rate the app.

## Integration
```kotlin
startActivity(Intent(context, SupportActivity::class.java))
```
