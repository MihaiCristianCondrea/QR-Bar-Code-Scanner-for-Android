# Help

## Layers
- **Data**: Supplies help links and support resources.
- **Domain**: Processes help requests and navigation events.
- **UI**: `HelpScreen` composable presented by `HelpActivity`.

## Primary Screens
- `HelpScreen` – displays FAQ entries and support contact options.

## Ads
The Help page displays a single native ad between the FAQ list and the Contact Us card.  
The banner is rendered by `HelpNativeAdBanner` which uses the shared `native_ad` configuration.  
An **Ad** label is shown to comply with policy requirements and padding ensures the banner
does not interfere with surrounding content.  
See [Ads](settings/privacy/ads.md) for more information on ad configuration.

## Integration
```kotlin
startActivity(Intent(context, HelpActivity::class.java))
```
