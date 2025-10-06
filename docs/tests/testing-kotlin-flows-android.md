Testing Kotlin flows on Android

bookmark_border
The way you test units or modules that communicate with flow depends on whether the subject under test uses the flow as input or output.

If the subject under test observes a flow, you can generate flows within fake dependencies that you can control from tests.
If the unit or module exposes a flow, you can read and verify one or multiple items emitted by a flow in the test.
Note: The Testing Kotlin coroutines on Android page describes the basics of working with the coroutine testing APIs.

Creating a fake producer
When the subject under test is a consumer of a flow, one common way to test it is by replacing the producer with a fake implementation. For example, given a class that observes a repository that takes data from two data sources in production:

the subject under test and the data layer
Figure 1. The subject under test and the data layer.
To make the test deterministic, you can replace the repository and its dependencies with a fake repository that always emits the same fake data:

dependencies are replaced with a fake implementation
Figure 2. Dependencies are replaced with a fake implementation.
To emit a predefined series of values in a flow, use the flow builder:


class MyFakeRepository : MyRepository {
    fun observeCount() = flow {
        emit(ITEM_1)
    }
}
In the test, this fake repository is injected, replacing the real implementation:


@Test
fun myTest() {
    // Given a class with fake dependencies:
    val sut = MyUnitUnderTest(MyFakeRepository())
    // Trigger and verify
    ...
}
Now that you have control over the outputs of the subject under test, you can verify that it works correctly by checking its outputs.

Note: You can use a similar fake repository for bigger tests such as UI tests. Replacing modules for testing depends on how you inject dependencies. See the Hilt testing guide to learn how to replace modules in tests using Hilt.
Asserting flow emissions in a test
If the subject under test is exposing a flow, the test needs to make assertions on the elements of the data stream.

Let's assume that the previous example's repository exposes a flow:

repository with fake dependencies that exposes a flow
Figure 3. A repository (the subject under test) with fake dependencies that exposes a flow.
With certain tests, you'll only need to check the first emission or a finite number of items coming from the flow.

You can consume the first emission to the flow by calling first(). This function waits until the first item is received and then sends the cancellation signal to the producer.


@Test
fun myRepositoryTest() = runTest {
    // Given a repository that combines values from two data sources:
    val repository = MyRepository(fakeSource1, fakeSource2)

    // When the repository emits a value
    val firstItem = repository.counter.first() // Returns the first item in the flow

    // Then check it's the expected item
    assertEquals(ITEM_1, firstItem)
}
If the test needs to check multiple values, calling toList() causes the flow to wait for the source to emit all its values and then returns those values as a list. This works only for finite data streams.


@Test
fun myRepositoryTest() = runTest {
    // Given a repository with a fake data source that emits ALL_MESSAGES
    val messages = repository.observeChatMessages().toList()

    // When all messages are emitted then they should be ALL_MESSAGES
    assertEquals(ALL_MESSAGES, messages)
}
For data streams that require a more complex collection of items or don't return a finite number of items, you can use the Flow API to pick and transform items. Here are some examples:


// Take the second item
outputFlow.drop(1).first()

// Take the first 5 items
outputFlow.take(5).toList()

// Takes the first item verifying that the flow is closed after that
outputFlow.single()

// Finite data streams
// Verify that the flow emits exactly N elements (optional predicate)
outputFlow.count()
outputFlow.count(predicate)
Continuous collection during a test
Collecting a flow using toList() as seen in the previous example uses collect() internally, and suspends until the entire result list is ready to be returned.

To interleave actions that cause the flow to emit values and assertions on the values that were emitted, you can continuously collect values from a flow during a test.

For example, take the following Repository class to be tested, and an accompanying fake data source implementation that has an emit method to produce values dynamically during the test:


class Repository(private val dataSource: DataSource) {
    fun scores(): Flow<Int> {
        return dataSource.counts().map { it * 10 }
    }
}

class FakeDataSource : DataSource {
    private val flow = MutableSharedFlow<Int>()
    suspend fun emit(value: Int) = flow.emit(value)
    override fun counts(): Flow<Int> = flow
}
When using this fake in a test, you can create a collecting coroutine that will continuously receive the values from the Repository. In this example, we're collecting them into a list and then performing assertions on its contents:


@Test
fun continuouslyCollect() = runTest {
    val dataSource = FakeDataSource()
    val repository = Repository(dataSource)

    val values = mutableListOf<Int>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        repository.scores().toList(values)
    }

    dataSource.emit(1)
    assertEquals(10, values[0]) // Assert on the list contents

    dataSource.emit(2)
    dataSource.emit(3)
    assertEquals(30, values[2])

    assertEquals(3, values.size) // Assert the number of items collected
}
Because the flow exposed by the Repository here never completes, the toList call that's collecting it never returns. Starting the collecting coroutine in TestScope.backgroundScope ensures that the coroutine gets cancelled before the end of the test. Otherwise, runTest would keep waiting for its completion, causing the test to stop responding and eventually fail.

Notice how UnconfinedTestDispatcher is used for the collecting coroutine here. This ensures that the collecting coroutine is launched eagerly and is ready to receive values after launch returns.

Using Turbine
The third-party Turbine library offers a convenient API for creating a collecting coroutine, as well as other convenience features for testing Flows:


@Test
fun usingTurbine() = runTest {
    val dataSource = FakeDataSource()
    val repository = Repository(dataSource)

    repository.scores().test {
        // Make calls that will trigger value changes only within test{}
        dataSource.emit(1)
        assertEquals(10, awaitItem())

        dataSource.emit(2)
        awaitItem() // Ignore items if needed, can also use skip(n)

        dataSource.emit(3)
        assertEquals(30, awaitItem())
    }
}
See the library's documentation for more details.

Testing StateFlows
StateFlow is an observable data holder, which can be collected to observe the values it holds over time as a stream. Note that this stream of values is conflated, which means that if values are set in a StateFlow rapidly, collectors of that StateFlow are not guaranteed to receive all intermediate values, only the most recent one.

In tests, if you keep conflation in mind, you can collect a StateFlow's values as you can collect any other flow, including with Turbine. Attempting to collect and assert on all intermediate values can be desirable in some test scenarios.

However, we generally recommend treating StateFlow as a data holder and asserting on its value property instead. This way, tests validate the current state of the object at a given point in time, and don't depend on whether or not conflation happens.

For example, take this ViewModel that collects values from a Repository and exposes them to the UI in a StateFlow:


class MyViewModel(private val myRepository: MyRepository) : ViewModel() {
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            myRepository.scores().collect { score ->
                _score.value = score
            }
        }
    }
}
Note: This ViewModel implementation uses a MutableStateFlow directly. To test StateFlow instances created with stateIn, see the next section.
A fake implementation for this Repository might look like this:


class FakeRepository : MyRepository {
    private val flow = MutableSharedFlow<Int>()
    suspend fun emit(value: Int) = flow.emit(value)
    override fun scores(): Flow<Int> = flow
}
When testing the ViewModel with this fake, you can emit values from the fake to trigger updates in the StateFlow of the ViewModel, and then assert on the updated value:


@Test
fun testHotFakeRepository() = runTest {
    val fakeRepository = FakeRepository()
    val viewModel = MyViewModel(fakeRepository)

    assertEquals(0, viewModel.score.value) // Assert on the initial value

    // Start collecting values from the Repository
    viewModel.initialize()

    // Then we can send in values one by one, which the ViewModel will collect
    fakeRepository.emit(1)
    assertEquals(1, viewModel.score.value)

    fakeRepository.emit(2)
    fakeRepository.emit(3)
    assertEquals(3, viewModel.score.value) // Assert on the latest value
}
Working with StateFlows created by stateIn
In the previous section, the ViewModel uses a MutableStateFlow to store the latest value emitted by a flow from the Repository. This is a common pattern, usually implemented in a simpler way by using the stateIn operator, which converts a cold flow into a hot StateFlow:


class MyViewModelWithStateIn(myRepository: MyRepository) : ViewModel() {
    val score: StateFlow<Int> = myRepository.scores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)
}
The stateIn operator has a SharingStarted parameter, which determines when it becomes active and starts consuming the underlying flow. Options such as SharingStarted.Lazily and SharingStarted.WhileSubscribed are frequently used in view models.

Caution: When testing a StateFlow created with such options, there must be at least one collector present during the test. Otherwise the stateIn operator doesn't start collecting the underlying flow, and the StateFlow's value will never be updated.
Even if you're asserting on the value of the StateFlow in your test, you'll need to create a collector. This can be an empty collector:


@Test
fun testLazilySharingViewModel() = runTest {
    val fakeRepository = HotFakeRepository()
    val viewModel = MyViewModelWithStateIn(fakeRepository)

    // Create an empty collector for the StateFlow
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.score.collect {}
    }

    assertEquals(0, viewModel.score.value) // Can assert initial value

    // Trigger-assert like before
    fakeRepository.emit(1)
    assertEquals(1, viewModel.score.value)

    fakeRepository.emit(2)
    fakeRepository.emit(3)
    assertEquals(3, viewModel.score.value)
}
Caution: As with any coroutine started in a test to collect a hot flow that never completes, this collecting coroutine needs to be cancelled manually at the end of the test unless you are using backgroundScope (as in the preceding code sample), which is automatically cancelled when the test finishes.
