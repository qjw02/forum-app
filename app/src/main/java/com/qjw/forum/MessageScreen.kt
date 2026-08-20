package com.qjw.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
    onOpenThread: (String) -> Unit
) {
    var loading by remember { mutableStateOf(UserStore.isLogin()) }
    var error by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (!UserStore.isLogin()) return@LaunchedEffect

        try {
            val result = ApiClient.api.getNotifications()
            if (result.code == 0) {
                notifications = result.data?.list.orEmpty()
            } else {
                error = result.message ?: "加载消息失败"
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

        when {
            !UserStore.isLogin() -> EmptyMessage("登录后查看论坛通知")

            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error.isNotEmpty() -> EmptyMessage(error)

            notifications.isEmpty() -> EmptyMessage("暂无新消息")

            else -> notifications.forEach { notification ->
                NotificationCard(notification, onOpenThread)
                Spacer(Modifier.height(10.dp))
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
