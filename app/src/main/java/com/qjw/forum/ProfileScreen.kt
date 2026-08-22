package com.qjw.forum

import android.content.Intent
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onReferral: () -> Unit,
    onMyThreads: () -> Unit,
    onMyReplies: () -> Unit,
    onDrafts: () -> Unit,
    onFriends: () -> Unit,
    onVip: () -> Unit
) {
    val context = LocalContext.current
    val uid = UserStore.getUid()
    val cachedProfile = remember(uid) { ProfileCache.get(uid) }
    var profile by remember(uid) { mutableStateOf(cachedProfile) }
    var loading by remember(uid) { mutableStateOf(cachedProfile == null) }
    var message by remember(uid) { mutableStateOf("") }
    var refreshing by remember(uid) { mutableStateOf(false) }
    var uploadingAvatar by remember(uid) { mutableStateOf(false) }
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

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            uploadingAvatar = true
            message = ""
            val file = File(context.cacheDir, "avatar_" + System.currentTimeMillis() + ".jpg")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IllegalStateException("无法读取所选图片")

                val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, body)
                val result = ApiClient.api.uploadAvatar(part)
                if (result.code == 0) {
                    val updatedProfile = profile?.copy(
                        avatar = profile?.avatar?.let {
                            it + "&t=" + System.currentTimeMillis()
                        }
                    )
                    if (updatedProfile != null) {
                        profile = updatedProfile
                        ProfileCache.save(updatedProfile)
                    } else {
                        ProfileCache.clear()
                        loadProfile()
                    }
                    message = "头像已更新"
                } else {
                    message = result.message ?: "头像上传失败"
                }
            } catch (e: Exception) {
                message = e.message ?: "头像上传失败"
            } finally {
                file.delete()
                uploadingAvatar = false
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

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            enabled = !uploadingAvatar,
                            onClick = { avatarPicker.launch("image/*") }
                        ) {
                            Text(if (uploadingAvatar) "头像上传中…" else "更换头像")
                        }

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
                ProfileAction("📝 本机草稿", onDrafts)
                Spacer(Modifier.height(10.dp))
                ProfileAction("👥 好友管理", onFriends)

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val websiteUrl = DomainManager.getDomain().trimEnd('/') + "/"
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                        )
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌐 网页版入口")
                        Text(
                            text = "更多功能请使用网页版",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

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
