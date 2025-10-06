# Recommendations for Android architecture

bookmark_border
This page presents several Architecture best practices and recommendations. Adopt them to improve your app’s quality, robustness, and scalability. They also make it easier to maintain and test your app.

> Note: You should treat the recommendations in the document as recommendations and not strict requirements. Adapt them to your app as needed.

The best practices below are grouped by topic. Each has a priority that reflects how strongly the team recommends it. The list of priorities is as follows:

- **Strongly recommended**: You should implement this practice unless it clashes fundamentally with your approach.
- **Recommended**: This practice is likely to improve your app.
- **Optional**: This practice can improve your app in certain circumstances.

> Note: In order to understand these recommendations, you should be familiar with the Architecture guidance.

## Layered architecture
Our recommended layered architecture favors separation of concerns. It drives UI from data models, complies with the single source of truth principle, and follows unidirectional data flow principles. Here are some best practices for layered architecture:

| Recommendation                                                    | Description                                                                                                                                                                                                                                                                                                                                                 |
|-------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Use a clearly defined data layer.                                 | **Strongly recommended**<br>The data layer exposes application data to the rest of the app and contains the vast majority of business logic of your app.<br>You should create repositories even if they just contain a single data source.<br>In small apps, you can choose to place data layer types in a data package or module.                          |
| Use a clearly defined UI layer.                                   | **Strongly recommended**<br>The UI layer displays the application data on the screen and serves as the primary point of user interaction.<br>In small apps, you can choose to place data layer types in a ui package or module.<br>More UI layer best practices here.                                                                                       |
| The data layer should expose application data using a repository. | **Strongly recommended**<br>Components in the UI layer such as composables, activities, or ViewModels shouldn't interact directly with a data source. Examples of data sources are:<br><br>- Databases, DataStore, SharedPreferences, Firebase APIs.<br>- GPS location providers.<br>- Bluetooth data providers.<br>- Network connectivity status provider. |
| Use coroutines and flows.                                         | **Strongly recommended**<br>Use coroutines and flows to communicate between layers.<br>More coroutines best practices here.                                                                                                                                                                                                                                 |
| Use a domain layer.                                               | **Recommended in big apps**<br>Use a domain layer, use cases, if you need to reuse business logic that interacts with the data layer across multiple ViewModels, or you want to simplify the business logic complexity of a particular ViewModel                                                                                                            |

## UI layer
The role of the UI layer is to display the application data on the screen and serve as the primary point of user interaction. Here are some best practices for the UI layer:

| Recommendation                                          | Description                                                                                                                                                                                                                                                                                                  |
|---------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Follow Unidirectional Data Flow (UDF).                  | **Strongly recommended**<br>Follow Unidirectional Data Flow (UDF) principles, where ViewModels expose UI state using the observer pattern and receive actions from the UI through method calls.                                                                                                              |
| Use AAC ViewModels if their benefits apply to your app. | **Strongly recommended**<br>Use AAC ViewModels to handle business logic, and fetch application data to expose UI state to the UI (Compose or Android Views).<br>See more ViewModel best practices here.<br><br>See the benefits of ViewModels here.                                                          |
| Use lifecycle-aware UI state collection.                | **Strongly recommended**<br>Collect UI state from the UI using the appropriate lifecycle-aware coroutine builder: `repeatOnLifecycle` in the View system and `collectAsStateWithLifecycle` in Jetpack Compose.<br>Read more about `repeatOnLifecycle`.<br><br>Read more about `collectAsStateWithLifecycle`. |
| Do not send events from the ViewModel to the UI.        | **Strongly recommended**<br>Process the event immediately in the ViewModel and cause a state update with the result of handling the event. More about UI events here.                                                                                                                                        |
| Use a single-activity application.                      | **Recommended**<br>Use Navigation Fragments or Navigation Compose to navigate between screens and deep link to your app if your app has more than one screen.                                                                                                                                                |
| Use Jetpack Compose.                                    | **Recommended**<br>Use Jetpack Compose to build new apps for phones, tablets and foldables and Wear OS.                                                                                                                                                                                                      |

The following snippet outlines how to collect the UI state in a lifecycle-aware manner:

### Views
```kotlin
class MyFragment : Fragment() {

    private val viewModel: MyViewModel by viewModel()

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

### Compose
```kotlin
// Compose snippet omitted for brevity
```

## ViewModel
ViewModels are responsible for providing the UI state and access to the data layer. Here are some best practices for ViewModels:

| Recommendation                                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|-----------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ViewModels should be agnostic of the Android lifecycle.   | **Strongly recommended**<br>ViewModels shouldn't hold a reference to any Lifecycle-related type. Don't pass Activity, Fragment, Context or Resources as a dependency. If something needs a Context in the ViewModel, you should strongly evaluate if that is in the right layer.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Use coroutines and flows.                                 | **Strongly recommended**<br>The ViewModel interacts with the data or domain layers using:<br><br>- Kotlin flows for receiving application data,<br>- `suspend` functions to perform actions using `viewModelScope`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Use ViewModels at screen level.                           | **Strongly recommended**<br>Do not use ViewModels in reusable pieces of UI. You should use ViewModels in:<br><br>- Screen-level composables,<br>- Activities/Fragments in Views,<br>- Destinations or graphs when using Jetpack Navigation.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Use plain state holder classes in reusable UI components. | **Strongly recommended**<br>Use plain state holder classes for handling complexity in reusable UI components. By doing this, the state can be hoisted and controlled externally.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| Do not use AndroidViewModel.                              | **Recommended**<br>Use the ViewModel class, not AndroidViewModel. The Application class shouldn't be used in the ViewModel. Instead, move the dependency to the UI or the data layer.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Expose a UI state.                                        | **Recommended**<br>ViewModels should expose data to the UI through a single property called `uiState`. If the UI shows multiple, unrelated pieces of data, the VM can expose multiple UI state properties.<br>You should make `uiState` a `StateFlow`.<br>You should create the `uiState` using the `stateIn` operator with the `WhileSubscribed(5000)` policy if the data comes as a stream of data from other layers of the hierarchy.<br>For simpler cases with no streams of data coming from the data layer, it's acceptable to use a `MutableStateFlow` exposed as an immutable `StateFlow`.<br>You can choose to have the `${Screen}UiState` as a data class that can contain data, errors and loading signals. This class could also be a sealed class if the different states are exclusive. |

The following snippet outlines how to expose UI state from a ViewModel:

```kotlin
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    newsRepository: NewsRepository
) : ViewModel() {

    val feedState: StateFlow<NewsFeedUiState> =
        newsRepository
            .getNewsResourcesStream()
            .mapToFeedState(savedNewsResourcesState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading
            )

    // ...
}
```

## Lifecycle
The following are some best practices for working with the Android lifecycle:

| Recommendation                                                | Description                                                                                                                                                                                                                                                             |
|---------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Do not override lifecycle methods in Activities or Fragments. | **Strongly recommended**<br>Do not override lifecycle methods such as `onResume` in Activities or Fragments. Use `LifecycleObserver` instead. If the app needs to perform work when the lifecycle reaches a certain `Lifecycle.State`, use the `repeatOnLifecycle` API. |

The following snippet outlines how to perform operations given a certain Lifecycle state:

### Views
```kotlin
class MyFragment: Fragment() {
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

### Compose
```kotlin
// Compose snippet omitted for brevity
```

## Handle dependencies
There are several best practices you should observe when managing dependencies between components:

| Recommendation                       | Description                                                                                                                                                                                                                                                                                                                         |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Use dependency injection.            | **Strongly recommended**<br>Use dependency injection best practices, mainly constructor injection when possible.                                                                                                                                                                                                                    |
| Scope to a component when necessary. | **Strongly recommended**<br>Scope to a dependency container when the type contains mutable data that needs to be shared or the type is expensive to initialize and is widely used in the app.                                                                                                                                       |
| Use Hilt.                            | **Recommended**<br>Use Hilt or manual dependency injection in simple apps. Use Hilt if your project is complex enough. For example, if you have:<br><br>- Multiple screens with ViewModels—integration<br>- WorkManager usage—integration<br>- Advance usage of Navigation, such as ViewModels scoped to the nav graph—integration. |

## Testing
The following are some best practices for testing:

| Recommendation         | Description                                                                                                                                                                                                                                                                                                                    |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Know what to test.     | **Strongly recommended**<br>Unless the project is roughly as simple as a hello world app, you should test it, at minimum with:<br><br>- Unit test ViewModels, including Flows.<br>- Unit test data layer entities. That is, repositories and data sources.<br>- UI navigation tests that are useful as regression tests in CI. |
| Prefer fakes to mocks. | **Strongly recommended**<br>Read more in the Use test doubles in Android documentation.                                                                                                                                                                                                                                        |
| Test StateFlows.       | **Strongly recommended**<br>When testing StateFlow:<br>- Assert on the `value` property whenever possible<br>- You should create a `collectJob` if using `WhileSubscribed`<br>For more information, check the *What to test in Android* DAC guide.                                                                             |

## Models
You should observe these best practices when developing models in your apps:

| Recommendation                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|-------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Create a model per layer in complex apps. | **Recommended**<br>In complex apps, create new models in different layers or components when it makes sense. Consider the following examples:<br><br>- A remote data source can map the model that it receives through the network to a simpler class with just the data the app needs<br>- Repositories can map DAO models to simpler data classes with just the information the UI layer needs.<br>- ViewModel can include data layer models in `UiState` classes. |

## Naming conventions
When naming your codebase, you should be aware of the following best practices:

| Recommendation                     | Description                                                                                                                                                                                                                                                                                                                                                                                                                      |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Naming methods.                    | **Optional**<br>Methods should be a verb phrase. For example, `makePayment()`.                                                                                                                                                                                                                                                                                                                                                   |
| Naming properties.                 | **Optional**<br>Properties should be a noun phrase. For example, `inProgressTopicSelection`.                                                                                                                                                                                                                                                                                                                                     |
| Naming streams of data.            | **Optional**<br>When a class exposes a Flow stream, LiveData, or any other stream, the naming convention is `get{model}Stream()`. For example, `getAuthorStream(): Flow<Author>` If the function returns a list of models the model name should be in the plural: `getAuthorsStream(): Flow<List<Author>>`.                                                                                                                      |
| Naming interfaces implementations. | **Optional**<br>Names for the implementations of interfaces should be meaningful. Have `Default` as the prefix if a better name cannot be found. For example, for a `NewsRepository` interface, you could have an `OfflineFirstNewsRepository`, or `InMemoryNewsRepository`. If you can find no good name, then use `DefaultNewsRepository`. Fake implementations should be prefixed with `Fake`, as in `FakeAuthorsRepository`. |

