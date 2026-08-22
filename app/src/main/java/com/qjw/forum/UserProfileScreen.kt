package com.qjw.forum

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileScreen(
    uid: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    var profile by remember(uid) { mutableStateOf<PublicProfileData?>(null) }
    var loading by remember(uid) { mutableStateOf(true) }
    var message by remember(uid) { mutableStateOf("") }
    var addingFriend by remember(uid) { mutableStateOf(false) }
    var showPrivateMessage by remember(uid) { mutableStateOf(false) }
    var privateMessage by remember(uid) { mutableStateOf("") }
    var sendingPrivateMessage by remember(uid) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        try {
            val result = ApiClient.api.getUserProfile(uid)
            if (result.code == 0 && result.data != null) {
                profile = result.data
            } else {
                message = result.message ?: "用户资料加载失败"
            }
        } catch (e: Exception) {
            message = e.message ?: "用户资料加载失败"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("返回")
        }

        Spacer(Modifier.height(12.dp))
        Text("用户中心", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        when {
            loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            profile != null -> {
                val user = requireNotNull(profile)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = user.username,
                            modifier = Modifier.size(88.dp)
                        )

                        Spacer(Modifier.height(10.dp))
                        Text(user.username, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = user.group_name ?: "普通会员",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStat("UID", user.uid.toString())
                            ProfileStat("积分", (user.credits ?: 0).toString())
                            ProfileStat("C币", (user.money ?: 0).toString())
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStat("主题", (user.threads ?: 0).toString())
                            ProfileStat("回复", (user.replies ?: 0).toString())
                            ProfileStat("收藏", (user.favorites ?: 0).toString())
                        }

                        user.regdate?.takeIf { it > 0 }?.let {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "注册于 " + SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                                ).format(Date(it * 1000)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = UserStore.isLogin() && !addingFriend && user.uid != UserStore.getUid(),
                    onClick = {
                        scope.launch {
                            addingFriend = true
                            try {
                                val result = ApiClient.api.friendAction(
                                    action = "add",
                                    uid = user.uid.toString()
                                )
                                message = result.message ?: if (result.code == 0) {
                                    "好友申请已发送"
                                } else {
                                    "操作失败"
                                }
                            } catch (e: Exception) {
                                message = e.message ?: "好友申请发送失败"
                            } finally {
                                addingFriend = false
                            }
                        }
                    }
                ) {
                    Text(
                        when {
                            !UserStore.isLogin() -> "登录后可添加好友"
                            addingFriend -> "正在发送申请…"
                            user.uid == UserStore.getUid() -> "这是我的主页"
                            else -> "👥 添加好友"
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = UserStore.isLogin() && user.uid != UserStore.getUid(),
                    onClick = {
                        // 清除上一次操作的旧提示，避免和本次私信发送混淆。
                        message = ""
                        privateMessage = ""
                        showPrivateMessage = true
                    }
                ) {
                    Text(
                        when {
                            !UserStore.isLogin() -> "登录后可发私信"
                            user.uid == UserStore.getUid() -> "不能给自己发私信"
                            else -> "✉️ 发私信"
                        }
                    )
                }

                if (message.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(message)
                }

                if (showPrivateMessage) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!sendingPrivateMessage) showPrivateMessage = false
                        },
                        title = { Text("发私信给 ${user.username}") },
                        text = {
                            OutlinedTextField(
                                value = privateMessage,
                                onValueChange = { privateMessage = it },
                                label = { Text("私信内容") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !sendingPrivateMessage
                            )
                        },
                        confirmButton = {
                            Button(
                                enabled = privateMessage.trim().isNotBlank() && !sendingPrivateMessage,
                                onClick = {
                                    scope.launch {
                                        sendingPrivateMessage = true
                                        try {
                                            val result = parsePrivateMessageResponse(
                                                ApiClient.api.sendPrivateMessage(
                                                    uid = user.uid.toString(),
                                                    message = privateMessage.trim()
                                                )
                                            )
                                            if (result.code == 0) {
                                                privateMessage = ""
                                                showPrivateMessage = false
                                                val plid = result.data?.plid
                                                if (plid.isNullOrBlank()) {
                                                    message = "私信已发送，可到消息中心查看"
                                                } else {
                                                    onOpenChat(plid)
                                                }
                                            } else {
                                                message = result.message ?: "私信发送失败"
                                            }
                                        } catch (e: Exception) {
                                            message = e.message ?: "私信发送失败，请稍后重试"
                                        } finally {
                                            sendingPrivateMessage = false
                                        }
                                    }
                                }
                            ) {
                                Text(if (sendingPrivateMessage) "发送中…" else "发送")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                enabled = !sendingPrivateMessage,
                                onClick = { showPrivateMessage = false }
                            ) {
                                Text("取消")
                            }
                        }
                    )
                }
            }

            else -> Text(message.ifBlank { "用户不存在" })
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
