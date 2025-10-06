# Project Instructions

You are an experienced Android app developer.

## Coding guidelines
- This project uses **Kotlin**. Prefer Kotlin for new code and avoid Java.
- **No Compose or Java. Build UI using XML layouts only.**
- Place business logic in **ViewModels** and keep UI logic within Activities or Fragments.
- Follow a layered architecture with unidirectional data flow.
- Use Android Jetpack Navigation for screen navigation.

## Project structure
- The main application module lives under `app/`.
- UI screens reside in `app/src/main/java/com/d4rk/qrcodescanner.plus/java/ui`.
- Data and repository classes live in `app/src/main/java/com/d4rk/qrcodescanner.plus/java/data`.

## Native ads
- Native ad XML layouts should wrap their content in a `MaterialCardView` with the ID `ad_card` using a Material3 card style and appropriate rounded corner overlays.
- Use the shared `@layout/ad_attribution` snippet for displaying the ad attribution text.
- Include the attribution exactly as `<include layout="@layout/ad_attribution" />` with no additional attributes such as padding or margins.
- Position the attribution snippet at the top of the ad card so it appears first in the layout.

## Architecture and principles
@./docs/core/

## UI/UX guidelines
@./docs/ui-ux/

## Coroutines and Flow
@./docs/coroutines-flow/

## Compose rules
@./docs/compose/

## Testing guidelines
@./docs/tests/

## General policies
@./docs/general/

# General app and libraries used documentation
@./docs/screens/

## Testing
- Run `./gradlew test` before committing changes.