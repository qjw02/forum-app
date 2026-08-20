package com.qjw.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.PostCard
import kotlinx.coroutines.launch

@Composable
fun ForumThreadListScreen(
    fid: String,
    onOpenThread: (String) -> Unit,
    onCreatePost: (String) -> Unit,
    onBack: () -> Unit
) {
    val cachedForum = remember(fid) { ContentCache.getForum(fid) }
    var forumData by remember(fid) { mutableStateOf(cachedForum?.data) }
    var loading by remember(fid) { mutableStateOf(cachedForum == null) }
    var message by remember(fid) { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun fetchThreads(version: String?) {
        scope.launch {
            try {
                val permission = ApiClient.api.checkPermission(
                    fid = fid,
                    uid = UserStore.getUid()
                )
                if (permission.code != 0 || permission.data?.allow != true) {
                    message = permission.message ?: "当前用户组或积分无权访问该板块"
                    return@launch
                }

                val result = ApiClient.api.getForumThreads(fid)
                if (result.code == 0 && result.data != null) {
                    forumData = result.data
                    version?.let { ContentCache.saveForum(fid, it, result.data) }
                } else {
                    message = result.message ?: "加载失败"
                }
            } catch (e: Exception) {
                message = e.message ?: "网络错误"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(fid) {
        scope.launch {
            try {
                val sync = ApiClient.api.getContentVersion("forum", fid)
                val version = sync.data?.version

                when {
                    sync.code != 0 || version.isNullOrBlank() -> {
                        if (cachedForum == null) fetchThreads(null) else loading = false
                    }
                    cachedForum?.version == version -> {
                        loading = false
                    }
                    else -> {
                        fetchThreads(version)
                    }
                }
            } catch (_: Exception) {
                if (cachedForum == null) fetchThreads(null) else loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("返回")
            }

            Button(onClick = { onCreatePost(fid) }) {
                Text("＋发布主题")
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            forumData?.list.isNullOrEmpty() -> Text(
                text = if (message.isNotEmpty()) message else "暂无帖子"
            )

            else -> {
                Text(
                    text = forumData?.name ?: "板块",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(forumData!!.list, key = { it.tid }) { post ->
                        PostCard(post = post, onClick = onOpenThread)
                    }
                }
            }
        }
    }
}
