# Jetpack Compose architectural layering

bookmark_border
This page provides a high-level overview of the architectural layers that make up Jetpack Compose, and the core principles that inform this design.

Jetpack Compose is not a single monolithic project; it is created from a number of modules which are assembled together to form a complete stack. Understanding the different modules that make up Jetpack Compose enables you to:

Use the appropriate level of abstraction to build your app or library
Understand when you can ‘drop down’ to a lower level for more control or customization
Minimize your dependencies

## Layers
The major layers of Jetpack Compose are:



Figure 1. The major layers of Jetpack Compose.

Each layer is built upon the lower levels, combining functionality to create higher level components. Each layer builds on public APIs of the lower layers to verify the module boundaries and enable you to replace any layer should you need to. Let's examine these layers from the bottom up.

### Runtime
This module provides the fundamentals of the Compose runtime such as remember, mutableStateOf, the @Composable annotation and SideEffect. You might consider building directly upon this layer if you only need Compose’s tree management abilities, not its UI.

### UI
The UI layer is made up of multiple modules ( ui-text, ui-graphics, ui-tooling, etc.). These modules implement the fundamentals of the UI toolkit, such as LayoutNode, Modifier, input handlers, custom layouts, and drawing. You might consider building upon this layer if you only need fundamental concepts of a UI toolkit.

### Foundation
This module provides design system agnostic building blocks for Compose UI, like Row and Column, LazyColumn, recognition of particular gestures, etc. You might consider building upon the foundation layer to create your own design system.

### Material
This module provides an implementation of the Material Design system for Compose UI, providing a theming system, styled components, ripple indications, icons. Build upon this layer when using Material Design in your app.

## Design principles
A guiding principle for Jetpack Compose is to provide small, focused pieces of functionality that can be assembled (or composed) together, rather than a few monolithic components. This approach has a number of advantages.

### Control
Higher level components tend to do more for you, but limit the amount of direct control that you have. If you need more control, you can "drop down" to use a lower level component.

For example, if you want to animate the color of a component you might use the animateColorAsState API:

```kotlin
val color = animateColorAsState(if (condition) Color.Green else Color.Red)
```

However, if you needed the component to always start out grey, you cannot do it with this API. Instead, you can drop down to use the lower level Animatable API:

```kotlin
val color = remember { Animatable(Color.Gray) }
LaunchedEffect(condition) {
    color.animateTo(if (condition) Color.Green else Color.Red)
}
```

The higher level animateColorAsState API is itself built upon the lower level Animatable API. Using the lower level API is more complex but offers more control. Choose the level of abstraction that best suits your needs.

### Customization
Assembling higher level components from smaller building blocks makes it far easier to customize components should you need to. For example, consider the implementation of Button provided by the Material layer:

```kotlin
@Composable
fun Button(
    // …
    content: @Composable RowScope.() -> Unit
) {
    Surface(/* … */) {
        CompositionLocalProvider(/* … */) { // set LocalContentAlpha
            ProvideTextStyle(MaterialTheme.typography.button) {
                Row(
                    // …
                    content = content
                )
            }
        }
    }
}
```

A Button is assembled from 4 components:

1. A material Surface providing the background, shape, click handling, etc.
2. A CompositionLocalProvider which changes the content’s alpha when the button is enabled or disabled
3. A ProvideTextStyle sets the default text style to use
4. A Row provides the default layout policy for the button's content

We have omitted some parameters and comments to make the structure clearer, but the entire component is only around 40 lines of code because it simply assembles these 4 components to implement the button. Components like Button are opinionated about which parameters they expose, balancing enabling common customizations against an explosion of parameters that can make a component harder to use. Material components, for example, offer customizations specified in the Material Design system, making it easy to follow material design principles.

If, however, you wish to make a customization beyond a component's parameters, then you can "drop down" a level and fork a component. For example, Material Design specifies that buttons should have a solid colored background. If you need a gradient background, this option is not supported by the Button parameters. In this case you can use the Material Button implementation as a reference and build your own component:

```kotlin
@Composable
fun GradientButton(
    // …
    background: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        // …
        modifier = modifier
            .clickable(onClick = {})
            .background(
                Brush.horizontalGradient(background)
            )
    ) {
        CompositionLocalProvider(/* … */) { // set material LocalContentAlpha
            ProvideTextStyle(MaterialTheme.typography.button) {
                content()
            }
        }
    }
}
```

The above implementation continues to use components from the Material layer, such as Material’s concepts of current content alpha and the current text style. However, it replaces the material Surface with a Row and styles it to achieve the desired appearance.

Caution: When dropping down to a lower layer to customize a component, ensure that you do not degrade any functionality by, for example, neglecting accessibility support. Use the component you are forking as a guide.
If you do not want to use Material concepts at all, for example if building your own bespoke design system, then you can drop down to purely using foundation layer components:

```kotlin
@Composable
fun BespokeButton(
    // …
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        // …
        modifier = modifier
            .clickable(onClick = {})
            .background(backgroundColor)
    ) {
        // No Material components used
        content()
    }
}
```

Jetpack Compose reserves the simplest names for the highest level components. For example, androidx.compose.material.Text is built upon androidx.compose.foundation.text.BasicText. This makes it possible to provide your own implementation with the most discoverable name if you wish to replace higher levels.

Caution: Forking a component means that you will not benefit from any future additions or bug fixes from the upstream component.

### Picking the right abstraction
Compose’s philosophy of building layered, reusable components means that you should not always reach for the lower level building blocks. Many higher level components not only offer more functionality but often implement best practices such as supporting accessibility.

For example, if you wanted to add gesture support to your custom component, you could build this from scratch using Modifier.pointerInput but there are other, higher level components built on top of this which may offer a better starting point, for example Modifier.draggable, Modifier.scrollable or Modifier.swipeable.

As a rule, prefer building on the highest-level component which offers the functionality you need in order to benefit from the best practices they include.

## Learn more
See the Jetsnack sample for an example of building a custom design system.

