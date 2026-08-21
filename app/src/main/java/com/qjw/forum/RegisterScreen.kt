package com.qjw.forum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Text(
            text = "注册账号",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("确认密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                when {
                    username.trim().isEmpty() || password.isEmpty() -> {
                        message = "用户名和密码不能为空"
                    }
                    password.length < 6 -> {
                        message = "密码至少需要 6 位"
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                        message = "请输入有效的邮箱地址"
                    }
                    else -> {
                        scope.launch {
                            submitting = true
                            message = ""

                            try {
                                val result = ApiClient.api.register(
                                    username.trim(),
                                    password,
                                    email.trim()
                                )

                                if (result.code == 0) {
                                    onSuccess()
                                } else {
                                    message = result.message ?: "注册失败"
                                }
                            } catch (e: Exception) {
                                message = when (e) {
                                    is retrofit2.HttpException -> "注册服务错误（${e.code()}）"
                                    is com.google.gson.JsonSyntaxException -> "注册接口返回异常，请确认服务器 register.php 已覆盖"
                                    else -> "注册请求失败：${e.message ?: "请检查网络"}"
                                }
                            } finally {
                                submitting = false
                            }
                        }
                    }
                }
            }
        ) {
            if (submitting) {
                CircularProgressIndicator()
            } else {
                Text("注册")
            }
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = onBack) {
            Text("返回登录")
        }

        if (message.isNotEmpty()) {
            Text(message)
        }
    }
}
