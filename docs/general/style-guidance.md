# Style Guidance

- Follow official Kotlin coding conventions.
- Prefer immutable `val` properties and keep functions small and focused.
- Name classes and files using `PascalCase`; use `camelCase` for functions and variables.
- Each file should end with a trailing newline.
- Compose UI uses Material 3 theming; reference `MaterialTheme` for colors, typography, and spacing.
- Use Kotlin Coroutines and Flow for asynchronous work and state streams.
- Inject dependencies with Koin; obtain ViewModels via Koin helpers.
