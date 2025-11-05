# Android Breakfast App (Skeleton)

This project provides an initial skeleton for the Android version of the Breakfast app.

## Structure

* `app` – the Android application module.
* `build.gradle` – root Gradle configuration.
* `settings.gradle` – includes the app module.
* `app/build.gradle` – module-level Gradle configuration with Jetpack Compose and network dependencies.
* `app/src/main/java/com/breakfast` – contains Kotlin source code.
  * `MainActivity.kt` – entry point using Compose.
  * `models` – data classes generated from the backend API.
  * `network` – Retrofit service and API client.
  * `ui` – placeholder UI screens for home and login.

## How to Run

1. Open this project in **Android Studio** (Giraffe or later).
2. Let Gradle sync and download dependencies.
3. Run on an emulator or physical device. Use `MainActivity` as the launch activity.

This skeleton includes sample models and network stubs. You can expand it by adding additional screens and service methods following the PRD.