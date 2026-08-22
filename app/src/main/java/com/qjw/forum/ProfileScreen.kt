package com.qjw.forum

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onReferral: () -> Unit,
    onMyThreads: () -> Unit,
    onMyReplies: () -> Unit,
    onFriends: () -> Unit,
    onVip: () -> Unit
) {
    val uid = UserStore.getUid()
    val cachedProfile = remember(uid) { ProfileCache.get(uid) }
    var profile by remember(uid) { mutableStateOf(cachedProfile) }
    var loading by remember(uid) { mutableStateOf(cachedProfile == null) }
    var message by remember(uid) { mutableStateOf("") }
    var refreshing by remember(uid) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadProfile() {
        scope.launch {
            refreshing = true
            message = ""
            try {
                val result = ApiClient.api.profile()
                if (result.code == 0 && result.data != null) {
                    profile = result.data
                    ProfileCache.save(result.data)
                } else {
                    message = result.message ?: "加载失败"
                }
            } catch (e: Exception) {
                message = e.message ?: "网络错误"
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    LaunchedEffect(uid) {
        if (cachedProfile == null) loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "我的",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        when {
            loading -> Box(
                modifier = Modifier.fillMaxWidth(),
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
                            contentDescription = null,
                            modifier = Modifier.size(90.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "用户组：" + (user.group_name ?: "普通会员"),
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("UID\n" + user.uid)
                            Text("积分\n" + (user.credits ?: 0))
                            Text("C币\n" + (user.money ?: 0))
                            Text("主题\n" + (user.threads ?: 0))
                            Text("帖子\n" + (user.posts ?: 0))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !refreshing,
                    onClick = { loadProfile() }
                ) {
                    Text(if (refreshing) "刷新资料中…" else "刷新资料")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onVip
                ) {
                    Text("⭐ 开通 VIP")
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onReferral
                ) {
                    Text("🎁 推广中心")
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "我的功能",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(10.dp))

                ProfileAction("📄 我的主题", onMyThreads)
                Spacer(Modifier.height(10.dp))
                ProfileAction("💬 我的回复", onMyReplies)
                Spacer(Modifier.height(10.dp))
                ProfileAction("👥 好友管理", onFriends)

                Spacer(Modifier.height(30.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        ProfileCache.clear()
                        UserStore.clear()
                        onLogout()
                    }
                ) {
                    Text("退出登录")
                }
            }

            else -> Text(message.ifEmpty { "暂无资料" })
        }

        if (message.isNotEmpty() && profile != null) {
            Spacer(Modifier.height(10.dp))
            Text(message)
        }
    }
}

@Composable
private fun ProfileAction(
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
