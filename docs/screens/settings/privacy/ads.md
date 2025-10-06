# Ads

## Layers
- **Data**: Implements data sources for advertising preferences.
- **Domain**: Contains use cases and business rules around ad configuration.
- **UI**: `AdsSettingsScreen` composable wrapped by `AdsSettingsActivity`.

## Primary Screens
- `AdsSettingsScreen` – allows users to manage ad related options.

## Components
- `HelpNativeAdBanner` – native ad used on the Help screen via the `native_ad` DI qualifier.
  See [Help page](../../help.md) for placement and policy details.

## Integration
```kotlin
startActivity(Intent(context, AdsSettingsActivity::class.java))
```
