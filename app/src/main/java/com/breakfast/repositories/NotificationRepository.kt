package com.breakfast.repositories

import android.content.Context
import com.breakfast.R
import com.breakfast.models.ApiResponse
import com.breakfast.models.NotificationModel
import com.breakfast.network.ApiService
import com.breakfast.utils.Result
import java.io.IOException

/**
 * Repository responsible for fetching and updating user notifications.
 * It abstracts the network layer from the view layer and wraps responses in a [Result]
 * to represent loading, success or error states.
 */
class NotificationRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * Retrieve the list of notifications for the current user.
     */
    suspend fun getNotifications(): Result<ApiResponse<List<NotificationModel>>> {
        return try {
            val list = apiService.notifications()
            Result.Success(list)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Mark a single notification as read.
     */
    suspend fun markAsRead(id: Int): Result<Unit> {
        return try {
            apiService.readNotification(id)
            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Mark all notifications as read.
     */
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            apiService.readAllNotifications()
            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }
}