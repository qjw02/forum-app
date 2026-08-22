package com.qjw.forum

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ForumFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushTokenManager.uploadToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"]
            ?: message.notification?.title
            ?: "QJWForum 有新消息"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: "点击查看详情"

        AppMessageNotifier.show(this, title, body)
    }
}
