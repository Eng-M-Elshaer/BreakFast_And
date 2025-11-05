package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.breakfast.models.ApiResponse
import com.breakfast.models.NotificationModel
import com.breakfast.repositories.NotificationRepository
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the state of notifications. It exposes the list of notifications
 * and actions to mark notifications as read or mark all as read. The results are wrapped
 * in a [Result] to provide loading, success and error states to the UI.
 */
class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {
    private val _notificationsState = MutableStateFlow<Result<ApiResponse<List<NotificationModel>>>?>(null)
    val notificationsState = _notificationsState.asStateFlow()

    private val _markState = MutableStateFlow<Result<Unit>?>(null)
    val markState = _markState.asStateFlow()

    /**
     * Fetch notifications from the repository. Updates [notificationsState].
     */
    fun fetchNotifications() {
        viewModelScope.launch {
            _notificationsState.value = Result.Loading
            val result = repository.getNotifications()
            _notificationsState.value = result
        }
    }

    /**
     * Mark a specific notification as read. Updates [markState].
     */
    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            _markState.value = Result.Loading
            val result = repository.markAsRead(id)
            _markState.value = result
        }
    }

    /**
     * Mark all notifications as read. Updates [markState].
     */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            _markState.value = Result.Loading
            val result = repository.markAllAsRead()
            _markState.value = result
        }
    }

    /** Clear notifications state (used to dismiss error dialogs without re-triggering) */
    fun clearNotificationsState() {
        _notificationsState.value = null
    }

    /** Clear mark-as-read state (used to dismiss error dialogs without re-triggering) */
    fun clearMarkState() {
        _markState.value = null
    }

    /**
     * Factory to create [NotificationViewModel] with required dependencies.
     */
    class Factory(private val apiService: com.breakfast.network.ApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
                val repository = NotificationRepository(apiService)
                @Suppress("UNCHECKED_CAST")
                return NotificationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}