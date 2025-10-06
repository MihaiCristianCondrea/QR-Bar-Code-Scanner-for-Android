# Main Activity

## Overview

`MainActivity` serves as the primary entry point of the application after the initial startup and
onboarding process. It hosts the main navigation structure and manages core UI components.

## Key Components

- **`ActivityMainBinding`**: View binding class for `activity_main.xml`.
- **`MainViewModel`**: ViewModel responsible for UI state and business logic related to the main
  screen.
- **Navigation**:
    - `NavHostFragment`: Hosts the navigation graph (`R.navigation.mobile_navigation`).
    - `NavController`: Manages app navigation.
    - `BottomNavigationView` (`nav_view`): Primary navigation for phones.
    - `NavigationRailView` (`nav_rail`): Primary navigation for tablets/larger screens.
    - `AppBarConfiguration`: Configures the Toolbar/Action Bar with the navigation graph.
- **UI Elements**:
    - `Toolbar`: The main app bar.
    - `AdView`: Displays advertisements.
- **Lifecycle Management**:
    - Handles consent information for ads.
    - Manages in-app updates using `AppUpdateManager`.
    - Prompts for in-app reviews using `ReviewHelper`.
    - Manages notifications for app updates and usage.

## Core Functionalities

- **Initialization**:
    - Sets up the splash screen.
    - Redirects to `StartupActivity` if onboarding is not complete.
    - Initializes ViewBinding and ViewModel.
    - Requests notification permissions on Android Tiramisu and above.
- **UI Setup**:
    - Configures the Action Bar.
    - Observes `MainViewModel` for UI state changes (e.g., theme, bottom navigation visibility,
      default tab).
    - Dynamically switches between `BottomNavigationView` and `NavigationRailView` based on screen
      width.
    - Handles navigation item selections and custom animations.
- **In-app Updates**:
    - Checks for and manages immediate in-app updates.
    - Shows notifications for downloaded updates.
- **In-app Reviews**:
    - Prompts users for an in-app review based on session count and prior prompts.
- **Ad Handling**:
    - Loads banner ads if consent is granted.
- **Back Press Handling**:
    - Implements a "press back again to exit" functionality.

## Interactions

- **`StartupActivity`**: Handles initial app setup and onboarding. If onboarding is not complete,
  `MainActivity` redirects to it.
- **`SupportActivity`**: Launched when the "Support" menu item is selected.
- **`BottomSheetMenuFragment`**: Shown when the navigation icon in the Toolbar is clicked, providing
  additional menu options.