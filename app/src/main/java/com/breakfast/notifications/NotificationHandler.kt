package com.breakfast.ui.tabbar.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.breakfast.managers.PreferenceManager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.breakfast.MainActivity
import com.breakfast.R
import java.net.URL

/**
 * Handles all notification creation and display logic.
 * Manages notification channels and formatting.
 */
class NotificationHandler(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /**
     * Fetch current FCM token and store it in shared preferences.
     * Can be called from Activity on app start.
     */
    fun fetchAndStoreFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "FCM current token: $token")
                    PreferenceManager(context).saveFcmToken(token)
                } else {
                    Log.w(TAG, "Failed to get FCM token: ${task.exception?.message}")
                }
            }
    }

    /**
     * Create notification channels for Android 8.0+.
     * Channels should be created once at app startup.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                },
                NotificationChannel(
                    CHANNEL_ORDERS,
                    "Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications about your orders"
                },
                NotificationChannel(
                    CHANNEL_PROMOTIONS,
                    "Promotions",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Promotional offers and deals"
                }
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    /**
     * Show a basic notification with title and body.
     */
    fun showNotification(
        title: String?,
        body: String?,
        imageUrl: String? = null,
        channelId: String = CHANNEL_GENERAL
    ) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: context.getString(R.string.app_name))
            .setContentText(body ?: "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Load and set image if provided
        imageUrl?.let { url ->
            try {
                val bitmap = loadImageFromUrl(url)
                bitmap?.let {
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(it)
                            .bigLargeIcon(null as Bitmap?) // Hide large icon when expanded
                    )
                    builder.setLargeIcon(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load notification image", e)
            }
        }

        val notificationId = generateNotificationId()
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Show an order-specific notification with high priority.
     */
    fun showOrderNotification(orderId: String?, status: String?) {
        val title = "Order Update"
        val message = when (status) {
            "confirmed" -> "Your order #$orderId has been confirmed"
            "preparing" -> "Your order #$orderId is being prepared"
            "ready" -> "Your order #$orderId is ready for pickup"
            "delivered" -> "Your order #$orderId has been delivered"
            else -> "Your order #$orderId has been updated"
        }

        showNotification(
            title = title,
            body = message,
            channelId = CHANNEL_ORDERS
        )
    }

    /**
     * Show a promotional notification with low priority.
     */
    fun showPromotionNotification(title: String?, message: String?) {
        showNotification(
            title = title ?: "Special Offer",
            body = message,
            channelId = CHANNEL_PROMOTIONS
        )
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    /**
     * Load image from URL synchronously.
     * Note: This should ideally be done with an image loading library like Coil or Glide.
     */
    private fun loadImageFromUrl(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URL", e)
            null
        }
    }

    companion object {
        private const val TAG = "NotificationHandler"
        private const val CHANNEL_GENERAL = "breakfast_general"
        private const val CHANNEL_ORDERS = "breakfast_orders"
        private const val CHANNEL_PROMOTIONS = "breakfast_promotions"
    }
}