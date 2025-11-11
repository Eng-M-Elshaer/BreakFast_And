## Breakfast Android App

Kotlin + Jetpack Compose Android application for Breakfast. Includes authentication flow, tabbed main experience, order management, and Firebase Cloud Messaging (FCM) notifications with Intent extras routing.

### Tech Stack
- Kotlin, Jetpack Compose, Material 3
- Navigation Compose
- Retrofit2 + OkHttp
- Firebase Cloud Messaging (FCM)
- Coroutines/Flows

## Project Structure
- `app` – main Android application module
  - `src/main/java/com/breakfast`
    - `MainActivity.kt` – entry Activity, hosts Compose UI and handles notification intents (onNewIntent)
    - `ui/navigation/NavGraph.kt` – app-level navigation graph (auth + main)
    - `ui/tabbar/main/MainScreen.kt` – bottom navigation host and feature routes
    - `ui/tabbar/history/HistoryDetailScreen.kt` – order history details
    - `ui/order/AddToOrderScreen.kt` – add items into an order
    - `notifications/NotificationHandler.kt` – notification builder/channels/permissions
    - `notifications/MyFirebaseMessagingService.kt` – FCM service: token + message handling
    - `notifications/NotificationNavRouter.kt` – SharedFlow bridge for runtime navigation from notifications
    - `managers/PreferenceManager.kt` – simple persistence (tokens, user)
    - `network/*` – `ApiClient` and service interfaces
    - `models/*` – data models
  - `src/main/AndroidManifest.xml` – app manifest and FCM service
- `build.gradle`, `settings.gradle` – Gradle configuration
- `app/build.gradle` – module-level config and dependencies

## Notifications Overview (FCM)
FCM notifications are handled in all app states (killed, background, foreground) without Android deep links. The app uses Intent extras to route after tapping a notification:

1) MyFirebaseMessagingService
   - Receives messages, maps `subject_type` and payload keys into a Bundle of navigation extras, then calls `NotificationHandler.showNotification(...)`.
   - Supported subjects:
     - `order_opened` → AddToOrder
     - `order_stoped` → Home
     - `order_reopened` → AddToOrder
     - `order_closed` → HistoryDetail
     - `promotion`/default → Notifications tab

2) NotificationHandler
   - Builds a PendingIntent targeting `MainActivity` with extras:
     - `nav_target`: one of `add_to_order`, `home`, `history`, `notifications`
     - `orderId`, `storeId` when applicable
   - Manages channels: `breakfast_general`, `breakfast_orders`, `breakfast_promotions`

3) MainActivity + Compose
   - `onNewIntent` forwards extras to `NotificationNavRouter`.
   - `MainScreen` consumes:
     - On startup: reads Activity intent extras once and navigates accordingly, then clears extras.
     - While running: listens to `NotificationNavRouter.events` and navigates immediately.

### Expected FCM Data Payload
Example JSON (HTTP v1 → Android data):

```json
{
  "message": {
    "token": "<device_fcm_token>",
    "data": {
      "subject_type": "order_opened",
      "order_id": "42",
      "store_id": "7",
      "status": "opened",
      "title": "Order Update",
      "message": "Order #42 opened"
    }
  }
}
```

Guidelines:
- Provide `order_id` (preferred) and `store_id` when routing to AddToOrder.
- For `order_closed`, provide `order_id` to route to HistoryDetail.
- If both `order_id` and `orderId` exist, `order_id` takes precedence (fallbacks supported).

## Navigation
- Authentication: Login, SignUp, Forgot/Verify/Reset, ChangePassword
- Main tabs: `home`, `notifications`, `history`, `profile`
- Feature routes:
  - `add_to_order/{orderId}/{storeId}`
  - `order_details/{orderId}`
  - `history_detail/{orderId}`
  - `custom_item/...`

## Permissions
- INTERNET, ACCESS_NETWORK_STATE
- POST_NOTIFICATIONS (Android 13+ runtime)
- CAMERA (optional)

## Setup
1) Open in Android Studio (Giraffe or later).
2) Ensure `google-services.json` exists in `app/`.
3) Let Gradle sync and download dependencies.
4) Use a device/emulator with Google Play services for FCM tests.

## Run
1) Select `app` configuration.
2) Run on device/emulator.
3) Useful Logcat tags: `FCMService`, `NotificationHandler`.

## Testing Notifications
- Firebase Console: send test message to device token.
- FCM HTTP v1 API: use the payload example above.
- Verify that tapping the notification navigates to the correct screen in all app states.

## Token Management
- On first launch (or permission grant on Android 13+), device token is fetched and saved via `PreferenceManager`.
- Use this token on backend to target this device.

## Troubleshooting
- No navigation after tapping:
  - Confirm `nav_target` + required IDs in extras (check Logcat).
  - Ensure device receives FCM (token valid, topic/device targeted).
- Android 13+ no notification:
  - Ensure POST_NOTIFICATIONS permission granted.
- Notification images:
  - `NotificationHandler` loads remote images synchronously for simplicity; prefer image loader/worker for large images.

## Contributing
- Keep composables small and testable.
- Prefer explicit typed navigation arguments.
- Keep network models separate from UI state.

## License
Proprietary – internal use for the Breakfast team unless stated otherwise.