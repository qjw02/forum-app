package com.qjw.forum

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object AppMessageNotifier {
    private const val CHANNEL_ID = "forum_messages"
    private const val CHANNEL_NAME = "消息提醒"

    fun notifyNewMessages(context: Context, newNotices: Int, newPrivateMessages: Int) {
        if (newNotices <= 0 && newPrivateMessages <= 0) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val parts = mutableListOf<String>()
        if (newNotices > 0) parts += "新回复/通知 " + newNotices + " 条"
        if (newPrivateMessages > 0) parts += "新私信 " + newPrivateMessages + " 条"

        val notification = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("QJWForum 有新消息")
            .setContentText(parts.joinToString("，"))
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
