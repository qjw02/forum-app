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
    var forumData by remember(fid) { mutableStateOf<ForumThreadData?>(null) }
    var loading by remember(fid) { mutableStateOf(true) }
    var message by remember(fid) { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadThreads() {
        scope.launch {
            loading = true
            message = ""

            try {
                val result = ApiClient.api.getForumThreads(fid)
                if (result.code == 0) {
                    forumData = result.data
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
        loadThreads()
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

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Text(
            text = forumData?.name ?: "板块",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(10.dp))

        if (forumData?.list.isNullOrEmpty()) {
            Text("暂无帖子")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(forumData!!.list, key = { it.tid }) { post ->
                    PostCard(
                        post = post,
                        onClick = onOpenThread
                    )
                }
            }
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}
