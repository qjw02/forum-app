package com.qjw.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DraftListScreen(
    onBack: () -> Unit,
    onEditDraft: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var drafts by remember { mutableStateOf(PostDraftStore.list(context)) }
    var forumNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            runCatching { ApiClient.api.getForums() }
                .getOrNull()
                ?.takeIf { it.code == 0 }
                ?.data
                ?.let { list ->
                    forumNames = list.associate { it.fid to it.name }
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
        Text("本机草稿", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "草稿仅保存在当前手机，图片需要重新选择。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        if (drafts.isEmpty()) {
            Text("暂无本机草稿")
        } else {
            drafts.forEach { item ->
                val draft = item.draft
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            draft.subject.ifBlank { "未填写标题" },
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            draft.message.ifBlank { "未填写内容" },
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "板块：" + (draft.forumId?.let { forumNames[it] ?: "板块 #$it" } ?: "未选择") + " · " +
                                if (draft.updatedAt > 0) {
                                    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                        .format(Date(draft.updatedAt))
                                } else {
                                    "较早保存"
                                },
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    PostDraftStore.clear(context, item.key)
                                    drafts = PostDraftStore.list(context)
                                }
                            ) { Text("删除") }
                            Button(onClick = { onEditDraft(item.key) }) { Text("继续编辑") }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
