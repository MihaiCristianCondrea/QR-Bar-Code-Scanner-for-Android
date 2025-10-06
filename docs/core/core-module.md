# Core Module

The **core** package provides foundational building blocks shared across AppToolkit features. It offers base domain models, UI abstractions, utility helpers and dependency injection qualifiers used throughout the library.

## Packages

### domain
Defines reusable result wrappers and UI state models, plus base use case interfaces for repositories and operations.

### ui
Hosts composable components and base classes like `ScreenViewModel` and `DefaultSnackbarHost` that standardize screen state handling and Snackbar presentation.

### utils
Includes helpers, extensions, constants and `DispatcherProvider` to access standard `CoroutineDispatcher` instances.

### di
Contains qualifiers such as `GithubToken` to assist dependency injection frameworks.

## Usage examples

### ScreenViewModel
```kotlin
class ExampleViewModel : ScreenViewModel<UiScreen, ExampleEvent, ExampleAction>(
    initialState = UiStateScreen(data = UiScreen())
) {
    // handle events
}
```

### DefaultSnackbarHost
```kotlin
val snackbarHostState = remember { SnackbarHostState() }

Scaffold(
    snackbarHost = { DefaultSnackbarHost(snackbarState = snackbarHostState) }
) { /* screen content */ }
```

### DispatcherProvider
```kotlin
class ExampleRepository(private val dispatchers: DispatcherProvider) {
    suspend fun load() = withContext(dispatchers.io) {
        /* blocking work */
    }
}
```

## See also

- [[Library]] – overview of all modules and features.
- [[Issue-Reporter-Module]] – demonstrates use of `ScreenViewModel` and networking helpers.
- [[Support-Module]] – integrates `DispatcherProvider` for billing and donation flows.
