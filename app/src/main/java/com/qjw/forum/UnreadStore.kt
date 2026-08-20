package com.qjw.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object UnreadStore {
    var notificationCount by mutableIntStateOf(0)
        private set

    var privateMessageCount by mutableIntStateOf(0)
        private set

    val totalCount: Int
        get() = notificationCount + privateMessageCount

    fun updateNotifications(count: Int) {
        notificationCount = count.coerceAtLeast(0)
    }

    fun updatePrivateMessages(count: Int) {
        privateMessageCount = count.coerceAtLeast(0)
    }

    fun clear() {
        notificationCount = 0
        privateMessageCount = 0
    }

    suspend fun refresh() {
        if (!UserStore.isLogin()) {
            clear()
            return
        }

        try {
            val notices = ApiClient.api.getNotifications()
            if (notices.code == 0) {
                updateNotifications(notices.data?.unread ?: 0)
            }
        } catch (_: Exception) {
        }

        try {
            val messages = ApiClient.api.getPrivateMessages()
            if (messages.code == 0) {
                updatePrivateMessages(messages.data?.unread ?: 0)
            }
        } catch (_: Exception) {
        }
    }
}
