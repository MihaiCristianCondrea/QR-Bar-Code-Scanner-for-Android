# UI Components

This page groups common Jetpack Compose components available in AppToolkit.

## Buttons

Use buttons to trigger actions. Compose offers `Button`, `OutlinedButton`, and `IconButton`.

AppToolkit wraps `IconButton`, `FilledIconButton`, `FilledTonalIconButton`, and `OutlinedIconButton` with
Material 3's expressive shape morphing via `IconButtonDefaults.shapes()`, providing round-to-square
transitions in response to interaction states.

```kotlin
Button(onClick = { /* handle action */ }) {
    Text("Submit")
}
```

- **Theming:** Colors and typography derive from `MaterialTheme`.
- **State management:** Enable or disable via a `Boolean` state and update the `onClick` action.

## Dialogs

Dialogs display critical information or request decisions.

```kotlin
var open by remember { mutableStateOf(true) }

if (open) {
    AlertDialog(
        onDismissRequest = { open = false },
        confirmButton = {
            TextButton(onClick = { open = false }) { Text("OK") }
        },
        title = { Text("Title") },
        text = { Text("Message") }
    )
}
```

- **Theming:** Dialog shapes and colors follow `MaterialTheme` values.
- **State management:** Track visibility with a mutable state variable.

## Form Fields

Collect user input with fields like `TextField` or `OutlinedTextField`.

```kotlin
var name by remember { mutableStateOf("") }

TextField(
    value = name,
    onValueChange = { name = it },
    label = { Text("Name") }
)
```

- **Theming:** Uses `MaterialTheme` for colors, shapes, and typography.
- **State management:** Manage field values with `remember` and `mutableStateOf` or a view-model.

## Layouts

Arrange UI elements with layout composables such as `Column`, `Row`, and `Box`.

```kotlin
Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    Text("Header", style = MaterialTheme.typography.titleLarge)
    Button(onClick = { /* action */ }) { Text("Tap") }
}
```

- **Theming:** Spacing and typography should reference `MaterialTheme` dimensions and text styles.
- **State management:** Layouts themselves hold no state but position stateful children.

## Feedback

Provide feedback with components like `Snackbar` or `CircularProgressIndicator`.

```kotlin
Scaffold { innerPadding ->
    SnackbarHost(hostState = remember { SnackbarHostState() })
    // content
}
```

- **Theming:** Adapts to `MaterialTheme` for colors and elevation.
- **State management:** `SnackbarHostState` controls message queue.

Return to [[Home]].
