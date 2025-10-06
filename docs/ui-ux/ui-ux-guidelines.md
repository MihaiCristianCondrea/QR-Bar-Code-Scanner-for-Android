# UI/UX Guidelines

This project follows modern Android design practices to create consistent and accessible experiences
across devices. Use these principles when designing new features.

## Adopt Material Design 3

- Implement layouts and components using Material Design 3.
- Leverage dynamic color, expressive shapes, and typography to convey hierarchy and personality.

## Provide a meaningful landing screen

- Present the app name and primary actions immediately.
- Give users a clear starting point to explore functionality.

## Delay sign-in prompts

- Offer as much functionality as possible before requiring authentication.
- Only prompt users to sign in when a feature truly requires it.

## Preserve user state

- Persist session data so users can seamlessly continue after navigating away or installing updates.

## Avoid redundant splash screens

- The platform already handles app launch visuals; additional splash screens slow users down.

## Use proper navigation

- The Up button moves within the app hierarchy and should never exit the app.
- The system Back button follows the user's history across screens.

## Keep UI consistent

- Maintain visual and behavioral consistency across screens and modules.
- Reuse components and patterns instead of creating divergent UIs.

## Enable sharing

- Provide explicit actions to share content or app links with other apps.

## Secure external links

- Open third-party content in a browser or WebView over HTTPS.

