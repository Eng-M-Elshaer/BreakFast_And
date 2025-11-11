package com.breakfast.notifications

import android.os.Bundle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Dispatches navigation intents coming from system notifications to the Compose layer.
 */
object NotificationNavRouter {
    private val _events = MutableSharedFlow<Bundle>(extraBufferCapacity = 1)
    val events: SharedFlow<Bundle> = _events

    fun dispatch(extras: Bundle) {
        _events.tryEmit(extras)
    }
}

