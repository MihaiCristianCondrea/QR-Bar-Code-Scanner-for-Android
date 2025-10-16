# UI Components

This page groups common Android View components used in AppToolkit.

## Buttons

Use buttons to trigger actions.

**XML**

```xml

<Button android:id="@+id/button_submit" android:layout_width="wrap_content"
    android:layout_height="wrap_content" android:text="Submit" />
```

**Kotlin**

```kotlin
val button = findViewById<Button>(R.id.button_submit)
button.setOnClickListener {
    // handle action
}
```

## Dialogs

Dialogs display critical information or request decisions.

```kotlin
AlertDialog.Builder(context)
    .setTitle("Title")
    .setMessage("Message")
    .setPositiveButton("OK") { _, _ -> }
    .show()
```

## Form Fields

Collect user input with `EditText`.

**XML**

```xml

<EditText android:id="@+id/edit_name" android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

**Kotlin**

```kotlin
val nameField = findViewById<EditText>(R.id.edit_name)
val name = nameField.text.toString()
```

## Layouts

Arrange UI elements with containers like `LinearLayout`.

**XML**

```xml

<androidx.appcompat.widget.LinearLayoutCompat android:layout_width="match_parent" android:layout_height="wrap_content"
    android:orientation="vertical" android:padding="16dp">
    <TextView android:id="@+id/header" android:layout_width="wrap_content"
        android:layout_height="wrap_content" android:text="Header" />
    <Button android:id="@+id/button_tap" android:layout_width="wrap_content"
        android:layout_height="wrap_content" android:text="Tap" />
</androidx.appcompat.widget.LinearLayoutCompat>
```

## Feedback

Provide feedback with components like `Snackbar` or progress indicators.

```kotlin
Snackbar.make(view, "Message", Snackbar.LENGTH_SHORT).show()
```

Return to [[Home]].