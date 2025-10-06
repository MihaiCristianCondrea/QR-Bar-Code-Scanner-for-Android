# Core Module

The **core** package provides building blocks shared across the app. It offers domain models,
ViewModel classes, utility helpers and dependency injection qualifiers used throughout the project.

## Packages

### domain

Houses use case classes and other business logic that operate on repositories.

### ui

Contains Activities, Fragments and ViewModels such as `MainViewModel`.

### utils

Provides helpers like `OpenSourceLicensesUtils` for launching the open source license screen,
`ReviewHelper` and `EdgeToEdgeDelegate`.

### di

Contains Hilt modules and qualifiers for dependency injection.

## Usage examples

### ViewModel```kotlin

class MainViewModel(
private val shouldShowStartupScreenUseCase: ShouldShowStartupScreenUseCase
) : ViewModel() {

    fun shouldShowStartupScreen(): Boolean = shouldShowStartupScreenUseCase()

}

```

### Snackbar helper
```kotlin
Snackbar.make(view, "Message", Snackbar.LENGTH_SHORT).show()
```

### ReviewHelper

```kotlin
ReviewHelper.launchInAppReviewIfEligible(activity, sessionCount, hasPromptedBefore) {
    // callback when review flow finishes
}
```

## See also

- [[Architecture]] – overview of app layers.
- [[Data Layer]] – repository and data source details.
- [[UI Components]] – common reusable views.