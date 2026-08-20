package com.qjw.forum

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object DomainManager {

    private const val CONFIG_URL = ApiConfig.CONFIG_URL
    private const val PREF = "domain_cache"
    private const val KEY_DOMAIN = "current_domain"

    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun getDomain(): String {
        val cachedDomain = context
            ?.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            ?.getString(KEY_DOMAIN, "")
            .orEmpty()
            .trim()
            .trimEnd('/')

        return cachedDomain.ifEmpty {
            ApiConfig.baseUrl.trim().trimEnd('/')
        }
    }

    private fun saveDomain(apiUrl: String, imageUrl: String? = null) {
        val normalizedApiUrl = apiUrl.trim().trimEnd('/')
        if (normalizedApiUrl.isEmpty()) return

        context
            ?.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_DOMAIN, normalizedApiUrl)
            ?.apply()

        ApiConfig.baseUrl = "$normalizedApiUrl/"
        imageUrl
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf { it.isNotEmpty() }
            ?.let { ApiConfig.imageUrl = "$it/" }

        ApiClient.reset()
    }

    suspend fun updateDomain() {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(CONFIG_URL)
                    .build()

                OkHttpClient().newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext

                    val body = response.body?.string().orEmpty()
                    val config = JSONObject(body)
                    val candidates = mutableListOf<Pair<String, String?>>()

                    config.optJSONObject("server")?.let { server ->
                        candidates += server.optString("api_url") to server.optString("image_url")
                    }

                    val backups = config.optJSONArray("backup_servers")
                    if (backups != null) {
                        for (index in 0 until backups.length()) {
                            backups.optJSONObject(index)?.let { server ->
                                candidates += server.optString("api_url") to server.optString("image_url")
                            }
                        }
                    }

                    candidates.firstOrNull { (apiUrl, _) ->
                        apiUrl.isNotBlank() && check(apiUrl)
                    }?.let { (apiUrl, imageUrl) ->
                        saveDomain(apiUrl, imageUrl)
                    }
                }
            } catch (_: Exception) {
                // Keep the last cached domain or ApiConfig.baseUrl as a fallback.
            }
        }
    }

    private fun check(domain: String): Boolean {
        return try {
            val normalizedDomain = domain.trim().trimEnd('/')
            val request = Request.Builder()
                .url("$normalizedDomain/api/home/index.php")
                .build()

            OkHttpClient().newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }
}
