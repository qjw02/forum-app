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
import androidx.compose.runtime.LaunchedEffect
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReferralScreen() {
    val context = LocalContext.current
    val uid = UserStore.getUid()
    val username = UserStore.getUsername().ifBlank { "好友" }
    var referralLink by remember(uid, username) {
        mutableStateOf(buildReferralLink(username, uid))
    }
    val linkReady = referralLink.isNotBlank()
    var copied by remember { mutableStateOf(false) }
    var visitCount by remember { mutableStateOf<Int?>(null) }
    var stats by remember { mutableStateOf<ReferralStatsData?>(null) }
    var loadingStats by remember { mutableStateOf(true) }
    var statsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        // 先使用本机已验证的地址，再刷新动态域名，避免推广链接出现未初始化状态。
        referralLink = buildReferralLink(username, uid)
        DomainManager.updateDomain()
        referralLink = buildReferralLink(username, uid)
    }

    LaunchedEffect(uid) {
        loadingStats = true
        statsError = null
        try {
            val result = ApiClient.api.getReferralStats()
            if (result.code == 0) {
                stats = result.data
                visitCount = result.data?.visit_count ?: 0
            } else {
                statsError = result.message ?: "推广数据暂时无法读取"
            }
        } catch (_: Exception) {
            statsError = "推广数据暂时无法读取，请稍后重试"
        } finally {
            loadingStats = false
        }
    }

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
                containerColor = Color(0xFFF4F0FF)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("推广数据", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = when {
                            loadingStats -> "正在读取推广数据..."
                            statsError != null -> statsError!!
                            else -> "推广 IP " + (visitCount ?: 0) + " · 注册会员 " + (stats?.registered_count ?: 0)
                        }
                    )
                }
                Text("📈", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F0FF)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("已记录奖励累计", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("金钱 +" + (stats?.total_money ?: 0) + "    C币 +" + (stats?.total_coin ?: 0) + "    贡献 +" + (stats?.total_contribution ?: 0))
                Spacer(Modifier.height(5.dp))
                Text("仅统计 APP 推广注册已记录的奖励；实际积分以论坛后台为准。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))

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
                    enabled = linkReady,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("推广链接", referralLink))
                        copied = true
                    }
                ) {
                    Text(
                        when {
                            !linkReady -> "正在生成推广链接..."
                            copied -> "已复制推广链接"
                            else -> "复制推广链接"
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "推广奖励",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        RewardCard(
            title = "好友访问站点",
            description = "如果您的朋友通过下面链接访问站点，您将获得积分奖励",
            rewards = "金钱 +10    推广IP点 +1"
        )

        Spacer(Modifier.height(12.dp))

        RewardCard(
            title = "好友注册成为会员",
            description = "如果您的朋友不但访问并且注册成为会员，您将再获得积分奖励",
            rewards = "金钱 +50    贡献 +1    C币 +10"
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "推广注册记录",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        if (loadingStats) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("正在读取注册记录...", modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (statsError != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(statsError!!, modifier = Modifier.padding(18.dp), color = Color(0xFFB3261E))
            }
        } else if (stats?.rewards.isNullOrEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("暂时没有已记录的推广注册。", modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            stats?.rewards?.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7FF))
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text(item.username ?: "新会员", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("金钱 +" + (item.money ?: 0) + "    C币 +" + (item.coin ?: 0) + "    贡献 +" + (item.contribution ?: 0))
                        item.dateline?.takeIf { it > 0 }?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "奖励时间：" + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it * 1000)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
        ReferralStep("3", "获得奖励", "奖励由论坛后台按推广规则自动发放。")

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
            enabled = linkReady,
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("推广链接", referralLink))
                copied = true
            }
        ) {
            Text(if (linkReady) "再次复制链接" else "正在生成推广链接...")
        }
    }
}

private fun buildReferralLink(username: String, uid: Int): String {
    val domain = DomainManager.getDomain()
        .trim()
        .trimEnd('/')

    return if (domain.startsWith("https://") || domain.startsWith("http://")) {
        "$username 邀请您访问成都千娇网 $domain/?fromuid=$uid"
    } else {
        ""
    }
}

@Composable
private fun RewardCard(
    title: String,
    description: String,
    rewards: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F0FF)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E368D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = rewards,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E368D)
            )
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
