package com.qjw.forum

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 从受控的 GitHub 配置文件读取服务器地址。
 * 配置只能提供 HTTPS 域名，APP 不下载或执行任何远程代码。
 */
object DomainManager {
    private const val CONFIG_URL = ApiConfig.CONFIG_URL
    private const val PREF = "domain_cache"
    private const val KEY_DOMAIN = "current_domain"
    private const val MAX_CONFIG_BYTES = 64 * 1024L

    private val secureClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun getDomain(): String {
        val cachedDomain = context
            ?.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            ?.getString(KEY_DOMAIN, "")
            .orEmpty()

        return normalizeHttpsOrigin(cachedDomain)
            ?: normalizeHttpsOrigin(ApiConfig.baseUrl)
            ?: ""
    }

    private fun saveDomain(apiUrl: String, imageUrl: String? = null) {
        val normalizedApiUrl = normalizeHttpsOrigin(apiUrl) ?: return
        val normalizedImageUrl = imageUrl?.let(::normalizeHttpsOrigin)

        context
            ?.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_DOMAIN, normalizedApiUrl)
            ?.apply()

        ApiConfig.baseUrl = "$normalizedApiUrl/"
        normalizedImageUrl?.let { ApiConfig.imageUrl = "$it/" }
        ApiClient.reset()
    }

    suspend fun updateDomain() {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(CONFIG_URL)
                    .header("Accept", "application/json")
                    .cacheControl(CacheControl.Builder().noCache().build())
                    .build()

                secureClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext
                    if ((response.body?.contentLength() ?: 0L) > MAX_CONFIG_BYTES) return@withContext

                    val body = response.body?.string().orEmpty()
                    if (body.length > MAX_CONFIG_BYTES || !body.trimStart().startsWith("{")) {
                        return@withContext
                    }

                    val config = JSONObject(body)
                    val candidates = mutableListOf<Pair<String, String?>>()

                    config.optJSONObject("server")?.let { server ->
                        candidates += server.optString("api_url") to server.optString("image_url")
                    }

                    config.optJSONArray("backup_servers")?.let { backups ->
                        for (index in 0 until backups.length()) {
                            backups.optJSONObject(index)?.let { server ->
                                candidates += server.optString("api_url") to server.optString("image_url")
                            }
                        }
                    }

                    candidates.firstOrNull { (apiUrl, _) ->
                        normalizeHttpsOrigin(apiUrl) != null && check(apiUrl)
                    }?.let { (apiUrl, imageUrl) ->
                        saveDomain(apiUrl, imageUrl)
                    }
                }
            } catch (_: Exception) {
                // 配置不可用时保留已验证的缓存域名，不接受任何降级地址。
            }
        }
    }

    /**
     * 仅允许标准 HTTPS 根域名，例如 https://forum.example.com。
     * 拒绝 HTTP、IP、内网地址、账号、端口、路径、查询串与片段。
     */
    private fun normalizeHttpsOrigin(value: String): String? {
        val url = value.trim().toHttpUrlOrNull() ?: return null
        if (url.scheme != "https") return null
        if (url.port != 443 || url.username.isNotEmpty() || url.password.isNotEmpty()) return null
        if (url.encodedPath !in setOf("", "/") || url.query != null || url.fragment != null) return null

        val host = url.host.lowercase()
        if (host == "localhost" || host.contains(":") || host.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))) {
            return null
        }
        if (!host.contains(".") || host.length > 253) return null

        return "https://$host"
    }

    private fun check(domain: String): Boolean {
        val normalizedDomain = normalizeHttpsOrigin(domain) ?: return false
        return try {
            val request = Request.Builder()
                .url("$normalizedDomain/api/home/index.php")
                .header("Accept", "application/json")
                .build()

            secureClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use false
                val body = response.body?.string().orEmpty().trimStart()
                body.startsWith("{")
            }
        } catch (_: Exception) {
            false
        }
    }
}
