# Help Screen

## Overview

The Help screen provides users with access to frequently asked questions (FAQs), options to submit
feedback, and links to important information like the app's privacy policy, terms of service, and
open-source licenses.

## Structure

The Help screen is implemented as an `Activity` that composes different sections:

- **`HelpActivity`**: The main activity for this screen. It manages the layout, renders the FAQ
  list, and hosts the feedback fragment.
- **FAQ list**: Built dynamically inside the activity by inflating a shared item layout for each
  question/answer pair.
- **`FeedbackFragment`**: Contains preferences related to user feedback, including an option to rate
  the app. This is loaded from `R.xml.preferences_feedback`.

## Features

The Help screen offers the following functionalities, accessible primarily through an options menu:

- **View FAQs**: Users can browse a list of common questions and answers, expanding items on demand
  to reveal the detailed responses.
- **Provide Feedback**: Users can initiate a review flow or be directed to the Google Play Store to
  leave a review.
- **View in Google Play**: Opens the app's listing on the Google Play Store.
- **Version Info**: Displays a dialog with the app's name, version, and copyright information.
- **Beta Program**: Opens a link to join the app's beta testing program on Google Play.
- **Terms of Service**: Opens a web link to the app's Terms of Service.
- **Privacy Policy**: Opens a web link to the app's Privacy Policy.
- **Open Source Licenses**: Displays a screen with a list of open-source libraries used in the app
  and their licenses.

## Integration

To launch the Help screen, use the following Kotlin code:

```kotlin
val intent = Intent(context, HelpActivity::class.java)
startActivity(intent)
```