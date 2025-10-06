# Architecture

The project is split into three top-level modules, each with a clear responsibility.

## app
- Android application layer.
- Hosts the UI and wires together features from the toolkit.

## core
- Shared business logic and utilities.
- Defines interfaces consumed by the app and data modules.

## data
- Handles persistence, network, and other data sources.
- Provides repository implementations for the core layer.

### Data Flow & Dependencies
- **Dependencies:** `app → core → data`
- **Data flow:** `data → core → app`

