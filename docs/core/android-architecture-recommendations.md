# Recommendations for Android architecture (Kotlin Projects)

This page presents several Architecture best practices and recommendations. Adopt them to improve
your app’s quality, robustness, and scalability. They also make it easier to maintain and test your
app.

**Note:** These recommendations are guidelines, not strict requirements. Adapt them to your app as
needed.

The best practices below are grouped by topic. Each has a priority that reflects how strongly the
team recommends it. The list of priorities is as follows:

* **Strongly recommended:** You should implement this practice unless it clashes fundamentally with
  your approach.
* **Recommended:** This practice is likely to improve your app.
* **Optional:** This practice can improve your app in certain circumstances.

Before adopting these recommendations, you should be familiar with the Architecture guidance.

## Layered architecture

Our recommended layered architecture favors separation of concerns. It drives the UI from data
models, complies with the single source of truth principle, and follows unidirectional data flow
principles. Here are some best practices for layered architecture:

| Recommendation                                                                                 | Description                                                                                                                                                                                                                                                                                                |
|:-----------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Use a clearly defined data layer.**<br/>Strongly recommended                                 | The data layer exposes application data to the rest of the app and contains the vast majority of business logic of your app.<br/>You should create repositories even if they just contain a single data source.<br/>In small apps, you can choose to place data layer types in a `data` package or module. |
| **Use a clearly defined UI layer.**<br/>Strongly recommended                                   | The UI layer displays the application data on the screen and serves as the primary point of user interaction.<br/>In small apps, you can choose to place UI layer types in a `ui` package or module.<br/>More UI layer best practices here.                                                                |
| **The data layer should expose application data using a repository.**<br/>Strongly recommended | Components in the UI layer such as Activities, Fragments, or ViewModels shouldn't interact directly with a data source. Examples of data sources are databases, SharedPreferences, Firebase APIs, GPS, Bluetooth, or network status providers.                                                             |
| **Use a domain layer.**<br/>Recommended in big apps                                            | Use a domain layer (use cases) if you need to reuse business logic that interacts with the data layer across multiple ViewModels, or you want to simplify the business logic complexity of a particular ViewModel.                                                                                         |

## UI layer

The role of the UI layer is to display the application data on the screen and serve as the primary
point of user interaction. Here are some best practices for the UI layer:

| Recommendation                                                                       | Description                                                                                                                                                   |
|:-------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Follow Unidirectional Data Flow (UDF).**<br/>Strongly recommended                  | Follow UDF principles, where ViewModels expose UI state using observable data holders and receive actions from the UI through function calls.                 |
| **Use AAC ViewModels if their benefits apply to your app.**<br/>Strongly recommended | Use AndroidX ViewModels to handle business logic and fetch application data to expose UI state to the UI.                                                     |
| **Use lifecycle-aware UI state collection.**<br/>Strongly recommended                | Use coroutines to collect UI state. When using Flows, collect them using `repeatOnLifecycle` so that the UI only collects the state when it is on the screen. |
| **Do not send events from the ViewModel to the UI.**<br/>Strongly recommended        | Process the event immediately in the ViewModel and cause a state update with the result of handling the event.                                                |
| **Use a single-activity application.**<br/>Recommended                               | Use Navigation Fragments to navigate between screens and deep link to your app if your app has more than one screen.                                          |

The following snippet outlines how to collect the UI state in a lifecycle-aware manner from a Kotlin
`Fragment`:

```kotlin
class MyFragment : Fragment() {

    private val viewModel: MyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    // Process item
                }
            }
        }
    }
}
```

## ViewModel

ViewModels are responsible for providing the UI state and access to the data layer. Here are some
best practices for ViewModels:

| Recommendation                                                                         | Description                                                                                                                                                                                                                               |
|:---------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ViewModels should be agnostic of the Android lifecycle.**<br/>Strongly recommended   | ViewModels shouldn't hold a reference to any lifecycle-related type. Don't pass `Activity`, `Fragment`, `Context`, or `Resources` as a dependency. If something needs a `Context` in the ViewModel, evaluate if it is in the right layer. |
| **Expose observable data.**<br/>Strongly recommended                                   | Use `StateFlow` or other observable data holders to expose application data from the ViewModel.                                                                                                                                           |
| **Use ViewModels at screen level.**<br/>Strongly recommended                           | Do not use ViewModels in reusable pieces of UI. Use ViewModels in screen-level Activities, Fragments, or destinations/graphs when using Jetpack Navigation.                                                                               |
| **Use plain state holder classes in reusable UI components.**<br/>Strongly recommended | Use plain state holder classes for handling complexity in reusable UI components. By doing this, the state can be hoisted and controlled externally.                                                                                      |
| **Do not use AndroidViewModel.**<br/>Recommended                                       | Use the `ViewModel` class, not `AndroidViewModel`. The `Application` class shouldn't be used in the ViewModel. Instead, move the dependency to the UI or the data layer.                                                                  |
| **Expose a UI state.**<br/>Recommended                                                 | ViewModels should expose data to the UI through a single `StateFlow` or other observable property representing the UI state.                                                                                                              |

The following snippet outlines how to expose UI state from a ViewModel in Kotlin:

```kotlin
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    newsRepository: NewsRepository
) : ViewModel() {

    private val _feedState = MutableStateFlow<NewsFeedUiState>(NewsFeedUiState.Loading)
    val feedState: StateFlow<NewsFeedUiState> = _feedState.asStateFlow()

    init {
        viewModelScope.launch {
            // fetch data from repository and update _feedState
        }
    }
}
```

## Lifecycle

The following are some best practices for working with the Android lifecycle:

| Recommendation                                                                             | Description                                                                                                                                                                                                                       |
|:-------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Do not override lifecycle methods in Activities or Fragments.**<br/>Strongly recommended | Do not override lifecycle methods such as `onResume` in Activities or Fragments. Use a `LifecycleObserver` instead. If the app needs to perform work when the lifecycle reaches a certain state, use the `repeatOnLifecycle` API. |

The following snippet outlines how to perform operations given a certain lifecycle state:

```kotlin
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                // ...
            }

            override fun onPause(owner: LifecycleOwner) {
                // ...
            }
        })
    }
}
```

## Handle dependencies

There are several best practices you should observe when managing dependencies between components:

| Recommendation                                                    | Description                                                                                                                                                                                                                                                   |
|:------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Use dependency injection.**<br/>Strongly recommended            | Use dependency injection best practices, mainly constructor injection when possible.                                                                                                                                                                          |
| **Scope to a component when necessary.**<br/>Strongly recommended | Scope to a dependency container when the type contains mutable data that needs to be shared or the type is expensive to initialize and is widely used in the app.                                                                                             |
| **Use Hilt.**<br/>Recommended                                     | Use Hilt or manual dependency injection in simple apps. Use Hilt if your project is complex enough. For example, if you have multiple screens with ViewModels, WorkManager usage, or advanced usage of Navigation such as ViewModels scoped to the nav graph. |

## Testing

The following are some best practices for testing:

| Recommendation                                             | Description                                                                                                                                                                                                                                                                            |
|:-----------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Know what to test.**<br/>Strongly recommended            | Unless the project is roughly as simple as a hello world app, you should test it, at minimum with: unit test ViewModels (including observable data), unit test data layer entities (repositories and data sources), and UI navigation tests that are useful as regression tests in CI. |
| **Prefer fakes to mocks.**<br/>Strongly recommended        | Read more in the “Use test doubles in Android” documentation.                                                                                                                                                                                                                          |
| **Test observable data holders.**<br/>Strongly recommended | When testing `StateFlow` or other observable data holders, collect from them and assert on emitted values.                                                                                                                                                                             |

## Models

You should observe these best practices when developing models in your apps:

| Recommendation                                                | Description                                                                                                                                                                                                                                                                                                                                                                                            |
|:--------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Create a model per layer in complex apps.**<br/>Recommended | In complex apps, create new models in different layers or components when it makes sense. For example, a remote data source can map the model it receives through the network to a simpler class with just the data the app needs; repositories can map DAO models to simpler data classes with just the information the UI layer needs; ViewModel can include data layer models in `UiState` classes. |

## Naming conventions

When naming your codebase, you should be aware of the following best practices:

| Recommendation                                      | Description                                                                                                                                                                                                                                                                                                                                                                                               |
|:----------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Naming functions.**<br/>Optional                  | Functions should be a verb phrase. For example, `makePayment()`.                                                                                                                                                                                                                                                                                                                                          |
| **Naming properties.**<br/>Optional                 | Properties should be a noun phrase. For example, `inProgressTopicSelection`.                                                                                                                                                                                                                                                                                                                              |
| **Naming streams of data.**<br/>Optional            | When a class exposes a stream such as a `Flow`, the naming convention is a noun representing the values. For example, `authors: Flow<List<Author>>`.                                                                                                                                                                                                                                                      |
| **Naming interfaces implementations.**<br/>Optional | Names for the implementations of interfaces should be meaningful. Use `Default` as the prefix if a better name cannot be found. For example, for a `NewsRepository` interface, you could have an `OfflineFirstNewsRepository` or `InMemoryNewsRepository`. If you can find no good name, use `DefaultNewsRepository`. Fake implementations should be prefixed with `Fake`, as in `FakeAuthorsRepository`. |