package com.qjw.forum

import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VipPurchaseScreen(onBack: () -> Unit) {
    val siteUrl = DomainManager.getDomain().trimEnd('/')
    val pluginUrl = "$siteUrl/plugin.php?id=threed_vip"
    var loading by remember { mutableStateOf(true) }
    var loginInProgress by remember { mutableStateOf(false) }
    var returnedToPlugin by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var profile by remember { mutableStateOf<ProfileData?>(null) }
    var profileError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val result = ApiClient.api.profile()
            if (result.code == 0) {
                profile = result.data
            } else {
                profileError = result.message ?: "无法读取当前身份"
            }
        } catch (_: Exception) {
            profileError = "无法读取当前身份"
        }
    }

    fun openPlugin() {
        loading = true
        returnedToPlugin = true
        webView?.loadUrl(pluginUrl)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onBack) {
                        Text("返回")
                    }
                    Button(
                        enabled = !loading,
                        onClick = { openPlugin() }
                    ) {
                        Text("进入 VIP 购买")
                    }
                }

                Text(
                    text = "开通 VIP",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = if (loginInProgress) {
                        "登录成功后将自动进入 VIP 购买页面。"
                    } else {
                        "VIP 套餐与支付由论坛现有插件提供。"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Text(
                    text = when {
                        profile != null -> "当前身份：" + (profile?.group_name ?: "普通会员") +
                            if (profile?.isVip == true) "（VIP）" else "（非 VIP）"
                        profileError != null -> profileError!!
                        else -> "正在读取当前身份…"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    CookieManager.getInstance().setAcceptCookie(true)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString().orEmpty()
                            if (url.contains("member.php?mod=logging")) {
                                loginInProgress = true
                                returnedToPlugin = false
                            }
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            loading = false
                            val currentUrl = url.orEmpty()
                            val cookies = CookieManager.getInstance()
                                .getCookie(siteUrl)
                                .orEmpty()

                            // Discuz 登录 Cookie 通常以 auth 结尾；检测到后只自动跳回一次。
                            if (
                                loginInProgress &&
                                !returnedToPlugin &&
                                !currentUrl.contains("plugin.php?id=threed_vip") &&
                                cookies.contains("auth=")
                            ) {
                                returnedToPlugin = true
                                loading = true
                                view?.loadUrl(pluginUrl)
                            }
                            super.onPageFinished(view, url)
                        }
                    }
                    loadUrl(pluginUrl)
                }
            }
        )
    }
}
