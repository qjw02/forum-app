package com.qjw.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import android.content.Context

object UnreadStore {
    private var appContext: Context? = null
    private var hasCompletedFirstRefresh = false

    fun init(context: Context) {
        appContext = context.applicationContext
    }

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
            hasCompletedFirstRefresh = false
            return
        }

        val oldNotices = notificationCount
        val oldPrivateMessages = privateMessageCount
        var newNotices = oldNotices
        var newPrivateMessages = oldPrivateMessages

        try {
            val notices = ApiClient.api.getNotifications()
            if (notices.code == 0) {
                newNotices = notices.data?.unread ?: 0
                updateNotifications(newNotices)
            }
        } catch (_: Exception) {
        }

        try {
            val messages = ApiClient.api.getPrivateMessages()
            if (messages.code == 0) {
                newPrivateMessages = messages.data?.unread ?: 0
                updatePrivateMessages(newPrivateMessages)
            }
        } catch (_: Exception) {
        }

        if (hasCompletedFirstRefresh) {
            AppMessageNotifier.notifyNewMessages(
                context = appContext ?: return,
                newNotices = (newNotices - oldNotices).coerceAtLeast(0),
                newPrivateMessages = (newPrivateMessages - oldPrivateMessages).coerceAtLeast(0)
            )
        }
        hasCompletedFirstRefresh = true
    }
}
