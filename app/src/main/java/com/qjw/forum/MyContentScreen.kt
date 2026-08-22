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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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

@Composable
fun MyContentScreen(
    showReplies: Boolean,
    onBack: () -> Unit,
    onOpenThread: (String, String?) -> Unit,
    onEditThread: (String) -> Unit
) {
    var loading by remember(showReplies) { mutableStateOf(true) }
    var error by remember(showReplies) { mutableStateOf("") }
    var threads by remember(showReplies) { mutableStateOf<List<MyThreadItem>>(emptyList()) }
    var replies by remember(showReplies) { mutableStateOf<List<MyReplyItem>>(emptyList()) }
    var deleteTarget by remember { mutableStateOf<MyThreadItem?>(null) }
    var deleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showReplies) {
        loading = true
        error = ""

        try {
            if (showReplies) {
                val result = ApiClient.api.getMyReplies()
                if (result.code == 0) {
                    replies = result.data?.list.orEmpty()
                } else {
                    error = result.message ?: "加载失败"
                }
            } else {
                val result = ApiClient.api.getMyThreads()
                if (result.code == 0) {
                    threads = result.data?.list.orEmpty()
                } else {
                    error = result.message ?: "加载失败"
                }
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
        Button(
            modifier = Modifier.padding(bottom = 4.dp),
            onClick = onBack
        ) {
            Text("返回")
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = if (showReplies) "我的回复" else "我的主题",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        when {
            loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error.isNotEmpty() -> Text(error)

            showReplies && replies.isEmpty() -> EmptyContent("还没有发表回复")

            !showReplies && threads.isEmpty() -> EmptyContent("还没有发布主题")

            showReplies -> replies.forEach { reply ->
                MyReplyCard(reply, onOpenThread)
                Spacer(Modifier.height(10.dp))
            }

            else -> threads.forEach { thread ->
                MyThreadCard(thread, onOpenThread, onEdit = { onEditThread(thread.tid) }, onDelete = { deleteTarget = thread })
                Spacer(Modifier.height(10.dp))
            }
        }

        deleteTarget?.let { thread ->
            AlertDialog(
                onDismissRequest = { if (!deleting) deleteTarget = null },
                title = { Text("删除主题？") },
                text = { Text("“${thread.subject}”删除后不能恢复。") },
                confirmButton = {
                    Button(
                        enabled = !deleting,
                        onClick = {
                            scope.launch {
                                deleting = true
                                try {
                                    val result = ApiClient.api.deletePost(thread.tid)
                                    if (result.code == 0) {
                                        threads = threads.filterNot { it.tid == thread.tid }
                                        deleteTarget = null
                                    } else {
                                        error = result.message ?: "删除失败"
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "删除失败"
                                } finally {
                                    deleting = false
                                }
                            }
                        }
                    ) { Text(if (deleting) "删除中…" else "确认删除") }
                },
                dismissButton = {
                    OutlinedButton(
                        enabled = !deleting,
                        onClick = { deleteTarget = null }
                    ) { Text("取消") }
                }
            )
        }
    }
}

@Composable
private fun MyThreadCard(
    thread: MyThreadItem,
    onOpenThread: (String, String?) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenThread(thread.tid, null) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(thread.subject, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "浏览 ${thread.views ?: 0} · 回复 ${thread.replies ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            androidx.compose.foundation.layout.Row {
                OutlinedButton(onClick = onEdit) { Text("编辑") }
                Spacer(Modifier.height(1.dp).weight(1f))
                OutlinedButton(onClick = onDelete) { Text("删除") }
            }
        }
    }
}

@Composable
private fun MyReplyCard(
    reply: MyReplyItem,
    onOpenThread: (String, String?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenThread(reply.tid, reply.pid) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reply.subject ?: "原主题已不存在",
                fontWeight = FontWeight.Bold
            )
            if (!reply.message.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = reply.message,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(text: String) {
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
