package com.qjw.forum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.ImageViewer
import kotlinx.coroutines.launch

@Composable
fun EditPostScreen(
    tid: String,
    onBack: () -> Unit,
    onSaved: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember(tid) { mutableStateOf(true) }
    var saving by remember(tid) { mutableStateOf(false) }
    var subject by remember(tid) { mutableStateOf("") }
    var message by remember(tid) { mutableStateOf("") }
    var originalImages by remember(tid) { mutableStateOf<List<String>>(emptyList()) }
    var resultText by remember(tid) { mutableStateOf("") }

    LaunchedEffect(tid) {
        try {
            val result = ApiClient.api.getThread(tid)
            if (result.code == 0 && result.data != null) {
                subject = result.data.thread.subject
                message = cleanEditableThreadText(
                    result.data.thread.rawContent ?: result.data.thread.content
                )
                originalImages = result.data.thread.images
            } else {
                resultText = result.message ?: "主题加载失败"
            }
        } catch (e: Exception) {
            resultText = e.message ?: "主题加载失败"
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
        Button(onClick = onBack) { Text("返回") }
        Spacer(Modifier.height(14.dp))
        Text("编辑主题", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            )
            if (originalImages.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("已有图片（保存修改后会保留）", style = MaterialTheme.typography.titleSmall)
                originalImages.forEach { imageUrl ->
                    Spacer(Modifier.height(8.dp))
                    ImageViewer(url = imageUrl)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("内容") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            )
            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving && subject.isNotBlank() && message.isNotBlank(),
                onClick = {
                    scope.launch {
                        saving = true
                        resultText = ""
                        try {
                            val result = ApiClient.api.editPost(tid, subject.trim(), message.trim())
                            if (result.code == 0) {
                                ContentCache.clearThread(tid)
                                PostCache.clear()
                                onSaved(tid)
                            } else {
                                resultText = result.message ?: "保存失败"
                            }
                        } catch (e: Exception) {
                            resultText = e.message ?: "保存失败"
                        } finally {
                            saving = false
                        }
                    }
                }
            ) { Text(if (saving) "保存中…" else "保存修改") }
        }

        if (resultText.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(resultText, color = MaterialTheme.colorScheme.error)
        }
    }
}


private fun cleanEditableThreadText(text: String): String {
    return text
        .replace(Regex("\\[i=s\\].*?\\[/i\\]\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\[attach\\].*?\\[/attach\\]", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\[/?(align|color|font|size|b|i|u|s)(=[^\\]]*)?\\]", RegexOption.IGNORE_CASE), "")
        .replace("&quot;", "\"")
        .trim()
}
