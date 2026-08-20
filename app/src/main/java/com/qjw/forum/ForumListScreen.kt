package com.qjw.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (cachedForums != null) return@LaunchedEffect

        scope.launch {
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
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Text(
            text = "板块",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(15.dp))

        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
