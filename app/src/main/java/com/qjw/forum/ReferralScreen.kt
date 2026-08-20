package com.qjw.forum

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReferralScreen() {
    val context = LocalContext.current
    val uid = UserStore.getUid()
    val domain = DomainManager.getDomain().trimEnd('/')
    val referralLink = domain + "/member.php?mod=register&referid=" + uid
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "推广中心",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "邀请好友加入论坛，获得推广奖励",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6950AF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "🎁 我的专属邀请",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Text("推广 UID：" + uid)

                Spacer(Modifier.height(14.dp))

                Text(
                    text = referralLink,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(18.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("推广链接", referralLink))
                        copied = true
                    }
                ) {
                    Text(if (copied) "已复制推广链接" else "复制推广链接")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "邀请方式",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        ReferralStep("1", "复制链接", "复制你的专属链接并发送给好友。")
        ReferralStep("2", "好友注册", "好友通过链接完成论坛注册。")
        ReferralStep("3", "获得奖励", "符合论坛推广规则后，奖励将自动发放。")

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "推广说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text("请勿使用虚假账号或刷量。具体奖励标准以论坛公告和后台规则为准。")
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("推广链接", referralLink))
                copied = true
            }
        ) {
            Text("再次复制链接")
        }
    }
}

@Composable
private fun ReferralStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            modifier = Modifier
                .background(
                    color = Color(0xFF6950AF),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
