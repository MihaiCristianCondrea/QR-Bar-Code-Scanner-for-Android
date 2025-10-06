# Data Layer

This page outlines how the app manages and persists data.

## Repositories

Repositories expose data to the rest of the app and hide the underlying storage.

```kotlin
interface MainRepository {
    fun shouldShowStartupScreen(): Boolean
    fun markStartupScreenShown()
}
```

`DefaultMainRepository` implements these methods using `SharedPreferences`:

```kotlin
class DefaultMainRepository(context: Context) : MainRepository {
    private val appContext = context.applicationContext

    override fun shouldShowStartupScreen(): Boolean {
        val startup = appContext.getSharedPreferences("startup", Context.MODE_PRIVATE)
        return startup.getBoolean("value", true)
    }

    override fun markStartupScreenShown() {
        val startup = appContext.getSharedPreferences("startup", Context.MODE_PRIVATE)
        startup.edit().putBoolean("value", false).apply()
    }
}
```

## Data sources

Remote and local sources supply the repositories with data. For example,
`DefaultHomeRemoteDataSource` uses Volley to fetch promoted apps:

```kotlin
class DefaultHomeRemoteDataSource(
    private val requestQueue: RequestQueue,
    private val apiUrl: String
) : HomeRemoteDataSource {

    override fun fetchPromotedApps(callback: PromotedAppsCallback) {
        val request = JsonObjectRequest(
            Request.Method.GET,
            apiUrl,
            null,
            { response -> /* parse and callback */ },
            { error -> /* handle error */ }
        )
        requestQueue.add(request)
    }
}
```

## Models

Model classes like `PromotedApp` encapsulate the data returned by the layer:

```kotlin
data class PromotedApp(val name: String, val packageName: String, val iconUrl: String)
```

## See also

- [[Architecture]] – overview of app layers.
- [[Core Module]] – shared utilities and components.