# Support

## Layers

- **Domain**: `SupportRepository` for managing product details and purchases. Handles donation
  logic.
- **UI**: `SupportActivity` (using `ActivitySupportBinding` and `SupportViewModel`) displays
  donation options, advertisements, and external links.
- **Billing**: Integrates `com.android.billingclient.api.BillingClient` for in-app purchases.
- **Ads**: `AdUtils` for loading banner advertisements.

## Primary Screens

- `SupportActivity` – allows users to make donations at different tiers, view advertisements, and
  visit an external support website.

## Integration

To open the support screen:

```kotlin
val intent = Intent(context, SupportActivity::class.java)
startActivity(intent)
```