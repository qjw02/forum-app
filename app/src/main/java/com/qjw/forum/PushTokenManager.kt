package com.qjw.forum

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushTokenManager {
    fun uploadCurrentToken() {
        if (!UserStore.isLogin()) return

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            uploadToken(token)
        }
    }

    fun uploadToken(token: String) {
        if (!UserStore.isLogin() || token.isBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.api.registerPushToken(token)
            } catch (_: Exception) {
                // 下次登录或 Token 更新时会再次上传。
            }
        }
    }
}
