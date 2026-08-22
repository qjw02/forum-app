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
    var hasUserEdited by remember(tid) { mutableStateOf(false) }
    val newImages = remember { mutableStateListOf<Uri>() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val editDraftKey = remember(tid) { "edit_draft_$tid" }

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
                PostDraftStore.load(context, editDraftKey)?.let { draft ->
                    if (draft.subject.isNotBlank() || draft.message.isNotBlank()) {
                        subject = draft.subject
                        message = draft.message
                        resultText = "已恢复上次未保存的编辑内容"
                    }
                }
            } else {
                resultText = result.message ?: "主题加载失败"
            }
        } catch (e: Exception) {
            resultText = e.message ?: "主题加载失败"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(hasUserEdited, subject, message, forumId) {
        if (hasUserEdited) {
            PostDraftStore.save(
                context,
                editDraftKey,
                PostDraft(
                    forumId = forumId?.toIntOrNull(),
                    subject = subject,
                    message = message,
                    sellContact = false,
                    contact = "",
                    price = "10"
                )
            )
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
                onValueChange = {
                    subject = it
                    hasUserEdited = true
                },
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
                onValueChange = {
                    message = it
                    hasUserEdited = true
                },
                label = { Text("内容") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "文字修改会自动保存为本机草稿；图片需重新选择。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                enabled = !saving,
                onClick = {
                    PostDraftStore.clear(context, editDraftKey)
                    hasUserEdited = false
                    resultText = "本机编辑草稿已清除（当前页面内容不会改变）"
                }
            ) {
                Text("清除本机编辑草稿")
            }
            Spacer(Modifier.height(10.dp))
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
                                    var attachment: String? = null
                                    var uploadError = "网络异常"
                                    for (attempt in 1..2) {
                                        try {
                                            if (attempt > 1) {
                                                uploadProgress = "新图片 $number/${newImages.size}：正在自动重试..."
                                            }
                                            val upload = ApiClient.api.uploadImage(part)
                                            val value = upload.data?.attachment
                                            if (upload.code == 0 && !value.isNullOrBlank()) {
                                                attachment = value
                                                break
                                            }
                                            uploadError = upload.message ?: "服务器未返回图片地址"
                                        } catch (error: Exception) {
                                            uploadError = error.message ?: "网络异常"
                                        }
                                    }
                                    val uploadedAttachment = attachment ?: throw IllegalStateException(
                                        "第 $number 张新图片上传失败（已自动重试 1 次），本次修改没有保存：$uploadError"
                                    )
                                    finalMessage += "\n\n[img]" +
                                        DomainManager.getDomain().trimEnd('/') +
                                        "/data/attachment/forum/" +
                                        uploadedAttachment +
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
                                PostDraftStore.clear(context, editDraftKey)
                                onSaved(tid)
                            } else {
                                resultText = "保存失败，当前编辑内容已保留，可直接重试：${result.message ?: "服务器未说明原因"}"
                            }
                        } catch (e: Exception) {
                            resultText = "保存失败，当前编辑内容已保留，可直接重试：${e.message ?: "网络异常"}"
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
