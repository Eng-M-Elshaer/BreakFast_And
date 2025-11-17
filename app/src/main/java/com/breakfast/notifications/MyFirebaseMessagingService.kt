package com.breakfast.notifications

import android.util.Log
import com.breakfast.managers.PreferenceManager
import com.breakfast.ui.tabbar.notifications.NotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.os.Bundle
import com.breakfast.models.NotificationSubjectType

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
    }

    /**
     * Called when a message is received from FCM.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Handle data payload (for silent notifications or custom data)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        } else {
            message.notification?.let { notification ->
            notificationHandler.showNotification(
                title = notification.title,
                body = notification.body,
                imageUrl = null
            ) }
        }
    }

    /**
     * Handle custom data payload from FCM.
     * This is useful for silent notifications or custom actions.
     */
    private fun handleDataPayload(data: Map<String, String>) {
        // Example: Handle different notification types
        when (data["subject_type"]) {
            NotificationSubjectType.OPEN.value -> {
                val title = data["title"]
                val message = data["message"]
                val subjectID = (data["subject_id"] ?: "0")
                val storeId = (data["store_id"] ?:  "0")

                val extras = Bundle().apply {
                    putString("nav_target", "add_to_order")
                    putInt("orderId", subjectID.toIntOrNull() ?: 0)
                    putInt("storeId", storeId.toIntOrNull() ?: 0)
                }
                notificationHandler.showNotification(
                    title = title,
                    body = message,
                    channelId = "breakfast_orders",
                    routeExtras = extras
                )
            }
            NotificationSubjectType.CLOSE.value -> {
                val subjectID = (data["subject_id"] ?: "0")

                val extras = Bundle().apply {
                    putString("nav_target", "history_detail")
                    putInt("orderId", subjectID.toIntOrNull() ?: 0)
                }
                notificationHandler.showNotification(
                    title = data["title"],
                    body = data["message"],
                    channelId = "breakfast_orders",
                    routeExtras = extras
                )
            }
            else -> {
                // Default notification
                val title = data["title"]
                val message = data["message"]
                val extras = Bundle().apply {
                    putString("nav_target", "home")
                }
                notificationHandler.showNotification(
                    title = title,
                    body = message,
                    channelId = "breakfast_general",
                    routeExtras = extras
                )
            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}