package com.qjw.forum

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.ImagePicker
import com.qjw.forum.component.ImagePreview
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun CreatePost(
    fid: String? = null,
    draftKeyOverride: String? = null,
    onOpenThread: (String) -> Unit
) {
    var forums by remember { mutableStateOf<List<ForumItem>>(emptyList()) }
    var selectedForum by remember { mutableStateOf<ForumItem?>(null) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var sellContact by remember { mutableStateOf(false) }
    var contact by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("10") }
    var resultText by remember { mutableStateOf("") }
    var publishing by remember { mutableStateOf(false) }
    var showForumMenu by remember { mutableStateOf(false) }
    var draftRestored by remember { mutableStateOf(false) }
    var draftStatus by remember { mutableStateOf("") }
    var checkingPostPermission by remember { mutableStateOf(false) }
    var canPublish by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf("") }
    var postPermissionMessage by remember { mutableStateOf("正在检查发布权限...") }
    val draftKey = remember(fid, draftKeyOverride) {
        draftKeyOverride ?: ("post_draft_" + (fid ?: "select"))
    }
    val images = remember { mutableStateListOf<Uri>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isAdvancedReport = selectedForum?.name == "高级报告"

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val result = ApiClient.api.getForums()
                if (result.code == 0) {
                    forums = result.data.orEmpty()
                    if (fid != null) {
                        selectedForum = forums.find { it.fid.toString() == fid }
                    }

                    PostDraftStore.load(context, draftKey)?.let { draft ->
                        subject = draft.subject
                        message = draft.message
                        sellContact = draft.sellContact
                        contact = draft.contact
                        priceText = draft.price
                        if (fid == null && draft.forumId != null) {
                            selectedForum = forums.find { it.fid == draft.forumId }
                        }
                        if (draft.subject.isNotBlank() || draft.message.isNotBlank()) {
                            resultText = "已恢复本机草稿（图片需重新选择）"
                        }
                    }
                }
            } catch (e: Exception) {
                resultText = e.message ?: "加载板块失败"
            } finally {
                draftRestored = true
            }
        }
    }

    LaunchedEffect(selectedForum?.fid) {
        val forum = selectedForum ?: return@LaunchedEffect
        checkingPostPermission = true
        canPublish = false
        try {
            val permissionResult = PermissionManager.request(forum.fid.toString())
            val permission = permissionResult.response
            if (permission?.code == 0 && permission.data?.allowPost == true) {
                canPublish = true
                postPermissionMessage = "允许在“${forum.name}”发布主题"
            } else {
                postPermissionMessage = permission?.message
                    ?: permissionResult.errorMessage
                    ?: "当前用户组无权在此板块发布主题"
            }
        } finally {
            checkingPostPermission = false
        }
    }

    LaunchedEffect(
        draftRestored,
        selectedForum?.fid,
        subject,
        message,
        sellContact,
        contact,
        priceText
    ) {
        if (!draftRestored) return@LaunchedEffect
        if (subject.isBlank() && message.isBlank() && contact.isBlank()) {
            PostDraftStore.clear(context, draftKey)
        } else {
            PostDraftStore.save(
                context,
                draftKey,
                PostDraft(
                    forumId = selectedForum?.fid,
                    subject = subject,
                    message = message,
                    sellContact = sellContact,
                    contact = contact,
                    price = priceText
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text("发布帖子", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (fid == null) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showForumMenu = !showForumMenu }
            ) {
                Text(selectedForum?.name ?: "选择发布板块")
            }

            if (showForumMenu) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        forums.forEach { forum ->
                            Text(
                                text = forum.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedForum = forum
                                        sellContact = false
                                        showForumMenu = false
                                    }
                                    .padding(14.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Text("发布到：" + (selectedForum?.name ?: "加载中..."))
        }

        if (selectedForum != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = postPermissionMessage,
                style = MaterialTheme.typography.bodySmall,
                color = if (canPublish) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("标题") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("内容") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )

        if (isAdvancedReport) {
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("出售联系方式")
                            Text(
                                "买家支付 C 币后才能查看",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = sellContact,
                            onCheckedChange = { sellContact = it }
                        )
                    }

                    if (sellContact) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contact,
                            onValueChange = { contact = it },
                            label = { Text("联系方式（微信、电话等）") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter(Char::isDigit) },
                            label = { Text("价格（C币）") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        ImagePicker(images = images, onChange = {})

        if (images.isNotEmpty()) {
            ImagePreview(
                images = images,
                onRemove = { index -> images.removeAt(index) }
            )
        }

        Spacer(Modifier.height(15.dp))

        Text(
            text = "内容会自动保存到本机草稿，图片需重新选择。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !publishing && (subject.isNotBlank() || message.isNotBlank() || contact.isNotBlank()),
            onClick = {
                PostDraftStore.save(
                    context,
                    draftKey,
                    PostDraft(
                        forumId = selectedForum?.fid,
                        subject = subject,
                        message = message,
                        sellContact = sellContact,
                        contact = contact,
                        price = priceText
                    )
                )
                draftStatus = "草稿已保存到本机"
            }
        ) {
            Text("保存草稿")
        }

        if (draftStatus.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                draftStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(8.dp))

        if (subject.isNotBlank() || message.isNotBlank() || contact.isNotBlank()) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !publishing,
                onClick = {
                    subject = ""
                    message = ""
                    sellContact = false
                    contact = ""
                    priceText = "10"
                    images.clear()
                    PostDraftStore.clear(context, draftKey)
                    resultText = "草稿已清空"
                }
            ) {
                Text("清空本机草稿")
            }
            Spacer(Modifier.height(10.dp))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !publishing && canPublish && !checkingPostPermission,
            onClick = {
                if (!canPublish) {
                    resultText = postPermissionMessage
                    return@Button
                }
                val forum = selectedForum
                if (forum == null) {
                    resultText = "请选择板块"
                    return@Button
                }
                if (sellContact && (contact.isBlank() || (priceText.toIntOrNull() ?: 0) <= 0)) {
                    resultText = "请填写联系方式和有效的 C 币价格"
                    return@Button
                }

                scope.launch {
                    publishing = true
                    try {
                        var finalMessage = message
                        images.forEachIndexed { index, uri ->
                            val number = index + 1
                            uploadProgress = "图片 $number/${images.size}：正在压缩..."
                            resultText = uploadProgress
                            val file = preparePostImage(context, uri, number)

                            try {
                                uploadProgress = "图片 $number/${images.size}：正在上传（${(number - 1) * 100 / images.size}%）"
                                resultText = uploadProgress
                                val body = UploadProgressRequestBody(
                                    file,
                                    "image/*".toMediaTypeOrNull()
                                ) { sent, total ->
                                    val current = if (total > 0) (sent * 100 / total).toInt() else 0
                                    val overall = ((index * 100) + current) / images.size
                                    uploadProgress = "图片 $number/${images.size}：上传中（$overall%）"
                                    resultText = uploadProgress
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
                                uploadProgress = "图片 $number/${images.size}：上传完成（${number * 100 / images.size}%）"
                                resultText = uploadProgress
                            } finally {
                                file.delete()
                            }
                        }

                        uploadProgress = ""
                        resultText = "正在发布，请稍候..."
                        val result = ApiClient.api.createPost(
                            fid = forum.fid.toString(),
                            subject = subject,
                            message = finalMessage,
                            contact = if (sellContact) contact.trim() else "",
                            price = if (sellContact) priceText.toIntOrNull() ?: 0 else 0
                        )

                        if (result.code == 0) {
                            PostCache.clear()
                            ContentCache.clearForum(forum.fid.toString())
                            PostDraftStore.clear(context, draftKey)
                            result.data?.tid?.let { onOpenThread(it.toString()) }
                        } else {
                            resultText = result.message ?: "发布失败"
                        }
                    } catch (e: Exception) {
                        resultText = if (e.message?.contains("timeout", ignoreCase = true) == true) {
                            "发布超时，请检查网络后重试"
                        } else {
                            e.message ?: "发布失败"
                        }
                    } finally {
                        publishing = false
                        uploadProgress = ""
                    }
                }
            }
        ) {
            Text(
                when {
                    checkingPostPermission -> "检查权限中..."
                    canPublish -> "发布"
                    else -> "当前无发布权限"
                }
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(resultText)
    }
}


fun preparePostImage(context: android.content.Context, uri: Uri, number: Int): File {
    val type = context.contentResolver.getType(uri).orEmpty()
    val suffix = if (type.equals("image/gif", ignoreCase = true)) ".gif" else ".jpg"
    val file = File(context.cacheDir, "post_" + System.currentTimeMillis() + "_" + number + suffix)

    if (suffix == ".gif") {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("无法读取所选 GIF 图片")
        return file
    }

    val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: throw IllegalStateException("无法读取所选图片")

    val maxSide = 1920
    val scale = minOf(1f, maxSide.toFloat() / maxOf(bitmap.width, bitmap.height).toFloat())
    val output = if (scale < 1f) {
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else bitmap

    FileOutputStream(file).use { stream ->
        output.compress(Bitmap.CompressFormat.JPEG, 82, stream)
    }
    if (output !== bitmap) output.recycle()
    bitmap.recycle()
    return file
}
