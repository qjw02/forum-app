package com.qjw.forum

import android.net.Uri

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.ImagePicker
import com.qjw.forum.component.ImagePreview
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
    var forumId by remember(tid) { mutableStateOf<String?>(null) }
    var originalImages by remember(tid) { mutableStateOf<List<String>>(emptyList()) }
    var uploadProgress by remember(tid) { mutableStateOf("") }
    var deletingImage by remember(tid) { mutableStateOf<String?>(null) }
    var resultText by remember(tid) { mutableStateOf("") }
    val newImages = remember { mutableStateListOf<Uri>() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(newImages.size) {
        if (newImages.size > 9) {
            while (newImages.size > 9) newImages.removeAt(newImages.lastIndex)
            resultText = "一次最多追加 9 张图片，超出部分已移除"
        }
    }

    LaunchedEffect(tid) {
        try {
            val result = ApiClient.api.getThread(tid)
            if (result.code == 0 && result.data != null) {
                subject = result.data.thread.subject
                forumId = result.data.thread.fid
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
                    OutlinedButton(
                        enabled = !saving,
                        onClick = { deletingImage = imageUrl }
                    ) {
                        Text("删除此图")
                    }
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
            Spacer(Modifier.height(14.dp))
            Text("追加图片", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            ImagePicker(images = newImages, onChange = {})
            if (newImages.isNotEmpty()) {
                ImagePreview(images = newImages, onRemove = { index -> newImages.removeAt(index) })
            }

            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving && subject.isNotBlank() && message.isNotBlank(),
                onClick = {
                    scope.launch {
                        saving = true
                        resultText = ""
                        try {
                            var finalMessage = message.trim()
                            newImages.forEachIndexed { index, uri ->
                                val number = index + 1
                                uploadProgress = "正在上传新图片 $number/${newImages.size}…"
                                val file = preparePostImage(context, uri, number)
                                try {
                                    val body = UploadProgressRequestBody(
                                        file,
                                        "image/*".toMediaTypeOrNull()
                                    ) { sent, total ->
                                        val current = if (total > 0) (sent * 100 / total).toInt() else 0
                                        val overall = ((index * 100) + current) / newImages.size
                                        uploadProgress = "正在上传新图片 $number/${newImages.size}（$overall%）"
                                    }
                                    val part = MultipartBody.Part.createFormData("file", file.name, body)
                                    val upload = ApiClient.api.uploadImage(part)
                                    if (upload.code != 0 || upload.data?.attachment.isNullOrBlank()) {
                                        throw IllegalStateException(upload.message ?: "图片上传失败")
                                    }
                                    finalMessage += "\n\n[img]" +
                                        DomainManager.getDomain().trimEnd('/') +
                                        "/data/attachment/forum/" +
                                        upload.data?.attachment +
                                        "[/img]"
                                } finally {
                                    file.delete()
                                }
                            }
                            uploadProgress = ""
                            val result = ApiClient.api.editPost(tid, subject.trim(), finalMessage)
                            if (result.code == 0) {
                                ContentCache.clearThread(tid)
                                forumId?.let { ContentCache.clearForum(it) }
                                PostCache.clear()
                                onSaved(tid)
                            } else {
                                resultText = result.message ?: "保存失败"
                            }
                        } catch (e: Exception) {
                            resultText = e.message ?: "保存失败"
                        } finally {
                            saving = false
                            uploadProgress = ""
                        }
                    }
                }
            ) { Text(if (saving) "保存中…" else "保存修改") }
        }

        deletingImage?.let { imageUrl ->
            AlertDialog(
                onDismissRequest = { if (!saving) deletingImage = null },
                title = { Text("删除图片？") },
                text = { Text("删除后无法恢复。") },
                confirmButton = {
                    Button(
                        enabled = !saving,
                        onClick = {
                            scope.launch {
                                saving = true
                                resultText = ""
                                try {
                                    val result = ApiClient.api.deleteThreadImage(tid, imageUrl)
                                    if (result.code == 0) {
                                        originalImages = originalImages.filterNot { it == imageUrl }
                                        message = removeImageCode(message, imageUrl)
                                        deletingImage = null
                                        ContentCache.clearThread(tid)
                                    } else {
                                        resultText = result.message ?: "图片删除失败"
                                    }
                                } catch (e: Exception) {
                                    resultText = e.message ?: "图片删除失败"
                                } finally {
                                    saving = false
                                }
                            }
                        }
                    ) { Text("确认删除") }
                },
                dismissButton = {
                    OutlinedButton(
                        enabled = !saving,
                        onClick = { deletingImage = null }
                    ) { Text("取消") }
                }
            )
        }

        if (uploadProgress.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(uploadProgress, color = MaterialTheme.colorScheme.primary)
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


private fun removeImageCode(text: String, imageUrl: String): String {
    val path = runCatching { java.net.URI(imageUrl).path }.getOrDefault(imageUrl)
    return text
        .replace("[img]$imageUrl[/img]", "")
        .replace("[img]$path[/img]", "")
        .trim()
}
