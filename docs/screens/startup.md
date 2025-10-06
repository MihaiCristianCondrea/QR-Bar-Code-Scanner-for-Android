# Startup

## Layers

- **ViewModel**: `StartupViewModel` handles the business logic for the startup screen, including
  consent information management.
- **UI**: `StartupActivity` displays the startup screen, handles user interactions, and presents
  options for privacy policy and proceeding to the app. It utilizes `ActivityStartupBinding` for
  view binding.
- **Utils**:
    - `ConsentRequestParameters`: Used to configure and request user consent.
    - `FastScrollerBuilder`: Enhances scrolling in the layout.

## Primary UI Components

- `ActivityStartupBinding`: Manages the views in `StartupActivity`.
- `buttonBrowsePrivacyPolicyAndTermsOfService`: Allows users to view the privacy policy and terms of
  service.
- `floatingButtonAgree`: Allows users to agree and navigate to `OnboardingActivity`.

## Integration & Navigation

- The `StartupActivity` is the initial activity launched.
- Upon user agreement, it navigates to `OnboardingActivity`.

```kotlin
// To start StartupActivity
val intent = Intent(context, StartupActivity::class.java)
context.startActivity(intent)

// Inside StartupActivity, on agreement:
startActivity(Intent(this, OnboardingActivity::class.java))
finish()
```