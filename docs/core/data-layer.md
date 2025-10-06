# Data Layer

This page outlines the data-related building blocks provided by AppToolkit.

## HTTP client

`apptoolkit/data/client` exposes **KtorClient**, a factory for a preconfigured Ktor `HttpClient`. It installs JSON content negotiation, request timeouts, default headers and optional logging so callers only need to supply their own endpoints.

### Setup

```kotlin
val client = KtorClient().createClient(enableLogging = true)
```

## DataStore

The `apptoolkit/data/datastore` package wraps Android DataStore in a singleton `CommonDataStore`. It centralizes preferences such as startup flags, theme options and user consents, exposing them as Kotlin `Flow`s with suspend functions to persist updates.

### Usage

```kotlin
val dataStore = CommonDataStore.getInstance(context)

// Observe a value
val adsEnabled = dataStore.ads(default = true)

// Save a value
scope.launch { dataStore.saveThemeMode("dark") }
```

## Ads

Ads are configured through preferences in `CommonDataStore` via the `ads` flag and related consent entries. The `core/ads` package provides `AdsCoreManager`, which checks those preferences before initializing Google Mobile Ads and manages app-open ad loading and display.

Use `AdsCoreManager` when the application should show an app-open ad on start or resume:

```kotlin
val adsManager = AdsCoreManager(context, buildInfoProvider)
scope.launch { adsManager.initializeAds("ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx") }
adsManager.showAdIfAvailable(activity, scope)
```

