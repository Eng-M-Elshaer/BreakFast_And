package com.breakfast.notifications

import android.util.Log
import com.breakfast.managers.PreferenceManager
import com.breakfast.ui.tabbar.notifications.NotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service that handles incoming push notifications
 * and token refresh events.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val notificationHandler by lazy {
        NotificationHandler(applicationContext)
    }

    /**
     * Called when a new FCM token is generated. This happens on initial app install
     * and whenever the token is refreshed.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")

        val prefs = PreferenceManager(applicationContext)
        prefs.saveFcmToken(token)

        // TODO: Send token to your backend server if needed
        // sendTokenToServer(token)
    }

    /**
     * Called when a message is received from FCM.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Handle notification payload
        message.notification?.let { notification ->
            notificationHandler.showNotification(
                title = notification.title,
                body = notification.body,
                imageUrl = notification.imageUrl?.toString()
            )
        }

        // Handle data payload (for silent notifications or custom data)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    /**
     * Handle custom data payload from FCM.
     * This is useful for silent notifications or custom actions.
     */
    private fun handleDataPayload(data: Map<String, String>) {
        // Example: Handle different notification types
        when (data["type"]) {
            "order_update" -> {
                val orderId = data["order_id"]
                val status = data["status"]
                notificationHandler.showOrderNotification(orderId, status)
            }
            "promotion" -> {
                val title = data["title"]
                val message = data["message"]
                notificationHandler.showPromotionNotification(title, message)
            }
            else -> {
                // Default notification
                notificationHandler.showNotification(
                    title = data["title"],
                    body = data["message"]
                )
            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}