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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MessageScreen(
    onOpenThread: (String) -> Unit,
    onOpenChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(UserStore.isLogin()) }
    var error by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<PrivateConversation>>(emptyList()) }

    LaunchedEffect(selectedTab) {
        if (!UserStore.isLogin()) return@LaunchedEffect
        loading = true
        error = ""

        try {
            if (selectedTab == 0) {
                val result = ApiClient.api.getNotifications()
                if (result.code == 0) {
                    notifications = result.data?.list.orEmpty()
                } else {
                    error = result.message ?: "加载消息失败"
                }
            } else {
                val result = ApiClient.api.getPrivateMessages()
                if (result.code == 0) {
                    conversations = result.data?.list.orEmpty()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "消息",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageTab("通知", selectedTab == 0) { selectedTab = 0 }
            MessageTab("私信", selectedTab == 1) { selectedTab = 1 }
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
                NotificationCard(notification, onOpenThread)
                Spacer(Modifier.height(10.dp))
            }

            else -> conversations.forEach { conversation ->
                ConversationCard(conversation, onOpenChat)
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
    onOpenChat: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat(conversation.plid) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = conversation.subject?.ifBlank { "私信" } ?: "私信",
                fontWeight = FontWeight.Bold
            )
            if (!conversation.message.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = conversation.message,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if ((conversation.unread ?: 0) > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "有未读消息",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onOpenThread: (String) -> Unit
) {
    val threadId = notification.from_id
        ?.takeIf { notification.from_idtype == "tid" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (threadId != null) {
                    Modifier.clickable { onOpenThread(threadId) }
                } else {
                    Modifier
                }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.type ?: "论坛通知",
                fontWeight = FontWeight.Bold
            )

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
