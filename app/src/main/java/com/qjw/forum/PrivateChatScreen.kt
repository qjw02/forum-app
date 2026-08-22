package com.qjw.forum

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivateChatScreen(
    plid: String,
    onBack: () -> Unit,
    onOpenUser: (String) -> Unit
) {
    val currentUid = UserStore.getUid().toString()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var loading by remember(plid) { mutableStateOf(true) }
    var error by remember(plid) { mutableStateOf("") }
    var messages by remember(plid) { mutableStateOf<List<PrivateChatItem>>(emptyList()) }
    var input by remember(plid) { mutableStateOf("") }
    var sending by remember(plid) { mutableStateOf(false) }

    suspend fun loadMessages() {
        try {
            val result = ApiClient.api.getPrivateMessageDetail(plid)
            if (result.code == 0) {
                messages = result.data?.list.orEmpty()
                error = ""
            } else {
                error = result.message ?: "加载聊天失败"
            }
        } catch (e: Exception) {
            error = e.message ?: "网络错误"
        } finally {
            loading = false
        }
    }

    // 仅在用户打开此聊天页时更新，避免在其他页面反复请求。
    LaunchedEffect(plid) {
        while (true) {
            loadMessages()
            delay(8_000)
        }
    }

    // 收到或发出新消息时，自动定位到最新消息。
    LaunchedEffect(messages.lastOrNull()?.pmid) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val otherUid = messages
        .firstOrNull { it.uid != currentUid }
        ?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Button(
            modifier = Modifier.padding(bottom = 4.dp),
            onClick = onBack
        ) {
            Text("返回")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "私信聊天",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (otherUid != null) {
                Button(onClick = { onOpenUser(otherUid) }) {
                    Text("查看资料")
                }
            }
        }
        Text(
            text = "聊天页面打开时自动更新",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error.isNotEmpty() && messages.isEmpty() -> Text(error)

            else -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                messages.forEach { chat ->
                    ChatBubble(
                        chat = chat,
                        isMine = chat.uid == currentUid
                    )
                }
            }
        }

        if (error.isNotEmpty() && messages.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("输入消息") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !sending && otherUid != null
        )

        Spacer(Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = input.isNotBlank() && otherUid != null && !sending,
            onClick = {
                val targetUid = otherUid ?: return@Button
                val message = input.trim()
                input = ""
                sending = true

                scope.launch {
                    try {
                        val result = ApiClient.api.sendPrivateMessage(targetUid, message)
                        if (result.code != 0) {
                            error = "发送失败，消息内容已保留，点击发送可重试：${result.message ?: "服务器未说明原因"}"
                            input = message
                        } else {
                            error = ""
                            loadMessages()
                        }
                    } catch (e: Exception) {
                        error = "发送失败，消息内容已保留，点击发送可重试：${e.message ?: "网络异常"}"
                        input = message
                    } finally {
                        sending = false
                    }
                }
            }
        ) {
            Text(if (sending) "发送中..." else "发送")
        }

        if (otherUid == null && messages.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "暂时无法识别聊天对象，不能发送新消息。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatBubble(
    chat: PrivateChatItem,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isMine) Color(0xFFDCD1FF) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = if (isMine) "我" else (chat.username ?: "对方"),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(chat.message.orEmpty())
            chat.dateline?.takeIf { it > 0 }?.let { seconds ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                        .format(Date(seconds * 1000)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
