package com.qjw.forum

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

@Composable
fun CreatePost(
    fid: String? = null,
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
                }
            } catch (e: Exception) {
                resultText = e.message ?: "加载板块失败"
            }
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

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !publishing,
            onClick = {
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
                            resultText = "正在上传图片 " + (index + 1) + "/" + images.size + "..."
                            val file = File(context.cacheDir, "upload_" + System.currentTimeMillis() + ".jpg")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }

                            val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val part = MultipartBody.Part.createFormData("file", file.name, body)
                            val upload = ApiClient.api.uploadImage(part)

                            if (upload.code == 0) {
                                finalMessage += "\n\n[img]" +
                                    DomainManager.getDomain().trimEnd('/') +
                                    "/data/attachment/forum/" +
                                    upload.data?.attachment +
                                    "[/img]"
                            }
                        }

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
                    }
                }
            }
        ) {
            Text("发布")
        }

        Spacer(Modifier.height(10.dp))
        Text(resultText)
    }
}
