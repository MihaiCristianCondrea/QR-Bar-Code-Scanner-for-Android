# IssueReporter

## Layers
- **Data**: Collects diagnostic information and user notes.
- **Domain**: Validates reports and prepares payloads.
- **UI**: `IssueReporterScreen` composable launched via `IssueReporterActivity`.

## Primary Screens
- `IssueReporterScreen` – form for sending bug reports.

## Integration
```kotlin
startActivity(Intent(context, IssueReporterActivity::class.java))
```
