package com.qjw.forum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Text(
            text = "QJWForum 登录",
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

        Spacer(Modifier.height(20.dp))

        Button(
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (username.trim().isEmpty() || password.isEmpty()) {
                    message = "用户名和密码不能为空"
                    return@Button
                }

                scope.launch {
                    submitting = true
                    message = ""

                    try {
                        val result = ApiClient.api.login(username.trim(), password)

                        if (result.code == 0 && result.data != null) {
                            UserStore.saveUser(
                                uid = result.data.uid,
                                username = result.data.username,
                                token = result.data.token
                            )
                            onLoginSuccess()
                        } else {
                            message = result.message ?: "登录失败"
                        }
                    } catch (e: Exception) {
                        message = e.message ?: "网络错误"
                    } finally {
                        submitting = false
                    }
                }
            }
        ) {
            if (submitting) {
                CircularProgressIndicator()
            } else {
                Text("登录")
            }
        }

        Spacer(Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton(onClick = onRegister) {
                Text("注册账号")
            }

            TextButton(onClick = onForgotPassword) {
                Text("忘记密码")
            }
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(message)
        }
    }
}
