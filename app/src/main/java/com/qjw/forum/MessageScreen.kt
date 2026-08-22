package com.qjw.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    onOpenThread: (String, String?) -> Unit,
    onOpenChat: (PrivateConversation) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(UserStore.isLogin()) }
    var error by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<PrivateConversation>>(emptyList()) }

    fun loadCurrentTab() {
        if (!UserStore.isLogin()) {
            loading = false
            return
        }
        scope.launch {
            loading = true
            error = ""

            try {
                if (selectedTab == 0) {
                    val result = ApiClient.api.getNotifications()
                    if (result.code == 0) {
                        notifications = result.data?.list.orEmpty()
                        UnreadStore.updateNotifications(result.data?.unread ?: 0)
                    } else {
                        error = result.message ?: "加载消息失败"
                    }
                } else {
                    val result = ApiClient.api.getPrivateMessages()
                    if (result.code == 0) {
                        conversations = result.data?.list.orEmpty()
                        UnreadStore.updatePrivateMessages(result.data?.unread ?: 0)
                    } else {
                        error = result.message ?: "加载私信失败"
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "网络错误"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        loadCurrentTab()
    }

    fun markRead(notification: NotificationItem) {
        if ((notification.isNew ?: 0) <= 0) return

        notifications = notifications.map {
            if (it.id == notification.id) it.copy(isNew = 0) else it
        }
        UnreadStore.updateNotifications((UnreadStore.notificationCount - 1).coerceAtLeast(0))

        scope.launch {
            try {
                ApiClient.api.getNotifications(readId = notification.id)
            } catch (_: Exception) {
            }
        }
    }

    fun openConversation(conversation: PrivateConversation) {
        val unread = conversation.unread ?: 0
        if (unread > 0) {
            conversations = conversations.map {
                if (it.plid == conversation.plid) it.copy(unread = 0) else it
            }
            UnreadStore.updatePrivateMessages(
                (UnreadStore.privateMessageCount - unread).coerceAtLeast(0)
            )
        }
        onOpenChat(conversation)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "消息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.OutlinedButton(
                enabled = !loading && UserStore.isLogin(),
                onClick = { loadCurrentTab() }
            ) {
                Text(if (loading) "更新中…" else "刷新")
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageTab(
                "通知" + unreadText(UnreadStore.notificationCount),
                selectedTab == 0
            ) { selectedTab = 0 }

            MessageTab(
                "私信" + unreadText(UnreadStore.privateMessageCount),
                selectedTab == 1
            ) { selectedTab = 1 }
        }

        Spacer(Modifier.height(16.dp))

        when {
            !UserStore.isLogin() -> EmptyMessage("登录后查看论坛通知和私信")

            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error.isNotEmpty() -> EmptyMessage(error)

            selectedTab == 0 && notifications.isEmpty() -> EmptyMessage("暂无新通知")

            selectedTab == 1 && conversations.isEmpty() -> EmptyMessage("暂无私信会话")

            selectedTab == 0 -> notifications.forEach { notification ->
                NotificationCard(notification, onOpenThread) { markRead(it) }
                Spacer(Modifier.height(10.dp))
            }

            else -> conversations.forEach { conversation ->
                ConversationCard(conversation) { openConversation(conversation) }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun MessageTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ConversationCard(
    conversation: PrivateConversation,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = conversation.avatar,
                contentDescription = null,
                modifier = Modifier.size(46.dp)
            )
            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.otherName?.ifBlank {
                            conversation.subject?.ifBlank { "私信" } ?: "私信"
                        } ?: conversation.subject?.ifBlank { "私信" } ?: "私信",
                        fontWeight = FontWeight.Bold
                    )
                    if ((conversation.unread ?: 0) > 0) {
                        Badge()
                    }
                }
                if (!conversation.message.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = conversation.message,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                conversation.dateline?.takeIf { it > 0 }?.let { seconds ->
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                            .format(Date(seconds * 1000)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onOpenThread: (String, String?) -> Unit,
    onRead: (NotificationItem) -> Unit
) {
    val threadId = notification.tid
        ?.takeIf { it.isNotBlank() && it != "0" }
        ?: notification.from_id?.takeIf { notification.from_idtype == "tid" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onRead(notification)
                if (threadId != null) {
                    onOpenThread(threadId, notification.pid)
                }
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notificationTitle(notification),
                    fontWeight = FontWeight.Bold
                )
                if ((notification.isNew ?: 0) > 0) {
                    Badge()
                }
            }

            if (!notification.note.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cleanDiscuzText(notification.note),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (threadId != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "点击查看相关主题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun notificationTitle(notification: NotificationItem): String {
    val username = notification.author?.username.orEmpty()

    return when (notification.type) {
        "post" -> if (username.isBlank()) "有人回复了你的帖子" else username + " 回复了你的帖子"
        "friend" -> if (username.isBlank()) "好友提醒" else username + " 添加了你为好友"
        "system" -> "系统通知"
        else -> notification.type ?: "论坛通知"
    }
}

private fun unreadText(count: Int): String {
    return if (count > 0) "（" + (if (count > 99) "99+" else count) + "）" else ""
}

@Composable
private fun EmptyMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
