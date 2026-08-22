package com.qjw.forum

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(onBack: () -> Unit, onOpenUser: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var uidInput by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var requests by remember { mutableStateOf<List<FriendItem>>(emptyList()) }

    suspend fun load() {
        loading = true
        try {
            val result = ApiClient.api.getFriends()
            if (result.code == 0) {
                friends = result.data?.friends.orEmpty()
                requests = result.data?.requests.orEmpty()
                message = ""
            } else {
                message = result.message ?: "加载失败"
            }
        } catch (e: Exception) {
            message = e.message ?: "网络错误"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    fun action(action: String, uid: String) {
        scope.launch {
            try {
                val result = ApiClient.api.friendAction(action, uid)
                message = result.message ?: if (result.code == 0) "操作成功" else "操作失败"
                if (result.code == 0) load()
            } catch (e: Exception) {
                message = e.message ?: "操作失败"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Button(onClick = onBack) { Text("返回") }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "好友管理",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = uidInput,
            onValueChange = { uidInput = it.filter(Char::isDigit) },
            label = { Text("输入对方 UID 添加好友") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = uidInput.isNotBlank(),
            onClick = {
                action("add", uidInput)
                uidInput = ""
            }
        ) {
            Text("发送好友申请")
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(message)
        }

        Spacer(Modifier.height(24.dp))

        Text("待处理好友申请", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (requests.isEmpty()) {
            Text("暂无好友申请")
        } else {
            requests.forEach { friend ->
                FriendRequestCard(friend, onOpenUser = { onOpenUser(friend.fuid) }) { action("accept", friend.fuid) }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("我的好友（" + friends.size + "）", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        if (!loading && friends.isEmpty()) {
            Text("暂时没有好友")
        }

        friends.forEach { friend ->
            FriendCard(friend, onOpenUser = { onOpenUser(friend.fuid) }) { action("delete", friend.fuid) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FriendIdentity(friend: FriendItem, compact: Boolean = false) {
    val nickname = friend.username ?: "用户 " + friend.fuid
    val avatarUrl = DomainManager.getDomain().trimEnd('/') +
        "/uc_server/avatar.php?uid=" + friend.fuid + "&size=middle"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(if (compact) 40.dp else 48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nickname.take(1),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            AsyncImage(
                model = avatarUrl,
                contentDescription = nickname + "的头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(nickname, fontWeight = FontWeight.Bold)
            Text(
                "UID " + friend.fuid + " · " + (friend.group_name ?: "普通会员"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FriendRequestCard(friend: FriendItem, onOpenUser: () -> Unit, onAccept: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable(onClick = onOpenUser)) {
                FriendIdentity(friend, compact = true)
            }
            Button(onClick = onAccept) { Text("同意") }
        }
    }
}

@Composable
private fun FriendCard(friend: FriendItem, onOpenUser: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable(onClick = onOpenUser)) {
                FriendIdentity(friend, compact = true)
            }
            OutlinedButton(onClick = onDelete) { Text("删除") }
        }
    }
}
