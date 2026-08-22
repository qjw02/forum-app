package com.qjw.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ForumListScreen(
    onOpenForum: (String) -> Unit
) {
    val cachedForums = remember { ForumCache.get() }
    var forums by remember { mutableStateOf(cachedForums.orEmpty()) }
    var loading by remember { mutableStateOf(cachedForums == null) }
    var message by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadForums() {
        scope.launch {
            refreshing = true
            message = ""
            try {
                val result = ApiClient.api.getForums()
                if (result.code == 0) {
                    forums = result.data.orEmpty()
                    ForumCache.save(forums)
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

    LaunchedEffect(Unit) {
        if (cachedForums == null) loadForums()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "板块",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                enabled = !refreshing,
                onClick = { loadForums() }
            ) {
                Text(if (refreshing) "刷新中…" else "刷新")
            }
        }

        Spacer(Modifier.height(15.dp))

        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            forums.isEmpty() -> Text("暂无板块")

            else -> LazyColumn {
                items(forums, key = { it.fid }) { forum ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                onOpenForum(forum.fid.toString())
                            }
                    ) {
                        Text(
                            text = forum.name,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Text(message)
        }
    }
}
