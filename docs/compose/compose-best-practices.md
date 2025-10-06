# Follow best practices

You might encounter common Compose pitfalls. These mistakes might give you code that seems to run well enough, but can hurt your UI performance. Follow best practices to optimize your app on Compose.

## Use `remember` to minimize expensive calculations
Composable functions can run very frequently, as often as for every frame of an animation. For this reason, you should do as little calculation in the body of your composable as you can.

An important technique is to store the results of calculations with `remember`. That way, the calculation runs once, and you can fetch the results whenever they're needed.

For example, here's some code that displays a sorted list of names, but does the sorting in a very expensive way:

```kotlin
@Composable
fun ContactList(
    contacts: List<Contact>,
    comparator: Comparator<Contact>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        // DON’T DO THIS
        items(contacts.sortedWith(comparator)) { contact ->
            // ...
        }
    }
}
```

Every time `ContactsList` is recomposed, the entire contact list is sorted all over again, even though the list hasn't changed. If the user scrolls the list, the composable gets recomposed whenever a new row appears.

To solve this problem, sort the list outside the `LazyColumn`, and store the sorted list with `remember`:

```kotlin
@Composable
fun ContactList(
    contacts: List<Contact>,
    comparator: Comparator<Contact>,
    modifier: Modifier = Modifier
) {
    val sortedContacts = remember(contacts, comparator) {
        contacts.sortedWith(comparator)
    }

    LazyColumn(modifier) {
        items(sortedContacts) {
            // ...
        }
    }
}
```

Now, the list is sorted once, when `ContactList` is first composed. If the `contacts` or `comparator` change, the sorted list is regenerated. Otherwise, the composable can keep using the cached sorted list.

> **Note:** If possible, it's best to move calculations outside of the composable altogether. In this case, you might want to sort the list elsewhere, like in your ViewModel, and provide the sorted list as an input to the composable.

## Use lazy layout keys
Lazy layouts efficiently reuse items, only regenerating or recomposing them when they have to. However, you can help optimize lazy layouts for recomposition.

Suppose a user operation causes an item to move in the list. For example, suppose you show a list of notes sorted by modification time with the most recently modified note on top.

```kotlin
@Composable
fun NotesList(notes: List<Note>) {
    LazyColumn {
        items(
            items = notes
        ) { note ->
            NoteRow(note)
        }
    }
}
```

There's a problem with this code though. Suppose the bottom note is changed. It's now the most recently modified note, so it goes to the top of the list, and every other note moves down one spot.

Without your help, Compose doesn't realize that unchanged items are just being moved in the list. Instead, Compose thinks the old "item 2" was deleted and a new one was created for item 3, item 4, and all the way down. The result is that Compose recomposes every item on the list, even though only one of them actually changed.

The solution here is to provide item keys. Providing a stable key for each item lets Compose avoid unnecessary recompositions. In this case, Compose can determine the item now at spot 3 is the same item that used to be at spot 2. Since none of the data for that item has changed, Compose doesn't have to recompose it.

```kotlin
@Composable
fun NotesList(notes: List<Note>) {
    LazyColumn {
        items(
            items = notes,
            key = { note ->
                // Return a stable, unique key for the note
                note.id
            }
        ) { note ->
            NoteRow(note)
        }
    }
}
```

## Use `derivedStateOf` to limit recompositions
One risk of using state in your compositions is that, if the state changes rapidly, your UI might get recomposed more than you need it to. For example, suppose you're displaying a scrollable list. You examine the list's state to see which item is the first visible item on the list:

```kotlin
val listState = rememberLazyListState()

LazyColumn(state = listState) {
    // ...
}

val showButton = listState.firstVisibleItemIndex > 0

AnimatedVisibility(visible = showButton) {
    ScrollToTopButton()
}
```

The problem here is, if the user scrolls the list, `listState` is constantly changing as the user drags their finger. That means the list is constantly being recomposed. However, you don't actually need to recompose it that often—you don't need to recompose until a new item becomes visible at the bottom. So, that's a lot of extra computation, which makes your UI perform badly.

The solution is to use derived state. `derivedStateOf` lets you tell Compose which changes of state actually should trigger recomposition. In this case, specify that you care about when the first visible item changes. When that state value changes, the UI needs to recompose, but if the user hasn't yet scrolled enough to bring a new item to the top, it doesn't have to recompose.

```kotlin
val listState = rememberLazyListState()

LazyColumn(state = listState) {
    // ...
}

val showButton by remember {
    derivedStateOf {
        listState.firstVisibleItemIndex > 0
    }
}

AnimatedVisibility(visible = showButton) {
    ScrollToTopButton()
}
```

## Defer reads as long as possible
When a performance issue has been identified, deferring state reads can help. Deferring state reads will ensure that Compose re-runs the minimum possible code on recomposition. For example, if your UI has state that is hoisted high up in the composable tree and you read the state in a child composable, you can wrap the state read in a lambda function. Doing this makes the read occur only when it is actually needed. For reference, see the implementation in the Jetsnack sample app. Jetsnack implements a collapsing-toolbar-like effect on its detail screen. To understand why this technique works, see the blog post [Jetpack Compose: Debugging Recomposition](https://medium.com/androiddevelopers/jetpack-compose-debugging-recomposition-33badf84eb5d).

To achieve this effect, the `Title` composable needs the scroll offset in order to offset itself using a `Modifier`. Here's a simplified version of the Jetsnack code before the optimization is made:

```kotlin
@Composable
fun SnackDetail() {
    // ...

    Box(Modifier.fillMaxSize()) { // Recomposition Scope Start
        val scroll = rememberScrollState(0)
        // ...
        Title(snack, scroll.value)
        // ...
    } // Recomposition Scope End
}

@Composable
private fun Title(snack: Snack, scroll: Int) {
    // ...
    val offset = with(LocalDensity.current) { scroll.toDp() }

    Column(
        modifier = Modifier
            .offset(y = offset)
    ) {
        // ...
    }
}
```

When the scroll state changes, Compose invalidates the nearest parent recomposition scope. In this case, the nearest scope is the `SnackDetail` composable. Note that `Box` is an inline function, and so is not a recomposition scope. So Compose recomposes `SnackDetail` and any composables inside `SnackDetail`. If you change your code to only read the state where you actually use it, then you could reduce the number of elements that need to be recomposed.

```kotlin
@Composable
fun SnackDetail() {
    // ...

    Box(Modifier.fillMaxSize()) { // Recomposition Scope Start
        val scroll = rememberScrollState(0)
        // ...
        Title(snack) { scroll.value }
        // ...
    } // Recomposition Scope End
}

@Composable
private fun Title(snack: Snack, scrollProvider: () -> Int) {
    // ...
    val offset = with(LocalDensity.current) { scrollProvider().toDp() }
    Column(
        modifier = Modifier
            .offset(y = offset)
    ) {
        // ...
    }
}
```

The `scroll` parameter is now a lambda. That means `Title` can still reference the hoisted state, but the value is only read inside `Title`, where it's actually needed. As a result, when the scroll value changes, the nearest recomposition scope is now the `Title` composable–Compose no longer needs to recompose the whole `Box`.

This is a good improvement, but you can do better! You should be suspicious if you are causing recomposition just to re-layout or redraw a composable. In this case, all you are doing is changing the offset of the `Title` composable, which could be done in the layout phase.

```kotlin
@Composable
private fun Title(snack: Snack, scrollProvider: () -> Int) {
    // ...
    Column(
        modifier = Modifier
            .offset { IntOffset(x = 0, y = scrollProvider()) }
    ) {
        // ...
    }
}
```

Previously, the code used `Modifier.offset(x: Dp, y: Dp)`, which takes the offset as a parameter. By switching to the lambda version of the modifier, you can make sure the function reads the scroll state in the layout phase. As a result, when the scroll state changes, Compose can skip the composition phase entirely, and go straight to the layout phase. When you are passing frequently changing `State` variables into modifiers, you should use the lambda versions of the modifiers whenever possible.

Here is another example of this approach. This code hasn't been optimized yet:

```kotlin
// Here, assume animateColorBetween() is a function that swaps between
// two colors
val color by animateColorBetween(Color.Cyan, Color.Magenta)

Box(
    Modifier
        .fillMaxSize()
        .background(color)
)
```

Here, the box's background color is switching rapidly between two colors. This state is thus changing very frequently. The composable then reads this state in the `background` modifier. As a result, the box has to recompose on every frame, since the color is changing on every frame.

To improve this, use a lambda-based modifier—in this case, `drawBehind`. That means the color state is only read during the draw phase. As a result, Compose can skip the composition and layout phases entirely—when the color changes, Compose goes straight to the draw phase.

```kotlin
val color by animateColorBetween(Color.Cyan, Color.Magenta)
Box(
    Modifier
        .fillMaxSize()
        .drawBehind {
            drawRect(color)
        }
)
```

## Avoid backwards writes
Compose has a core assumption that you will never write to state that has already been read. When you do this, it is called a backwards write and it can cause recomposition to occur on every frame, endlessly.

The following composable shows an example of this kind of mistake.

```kotlin
@Composable
fun BadComposable() {
    var count by remember { mutableIntStateOf(0) }

    // Causes recomposition on click
    Button(onClick = { count++ }, Modifier.wrapContentSize()) {
        Text("Recompose")
    }

    Text("$count")
    count++ // Backwards write, writing to state after it has been read
}
```

This code updates the count at the end of the composable after reading it on the preceding line. If you run this code, you'll see that after you click the button, which causes a recomposition, the counter rapidly increases in an infinite loop as Compose recomposes this composable, sees a state read that is out of date, and so schedules another recomposition.

You can avoid backwards writes altogether by never writing to state in composition. If at all possible, always write to state in response to an event and in a lambda like in the preceding `onClick` example.

