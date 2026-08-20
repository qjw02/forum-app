package com.qjw.forum

import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val APP_SECRET = "APP_SECRET_2026"

    private var retrofit: Retrofit? = null
    private var currentUrl: String = ""

    fun init() = reset()

    val api: ApiService
        get() {
            val domain = DomainManager.getDomain()
            require(domain.isNotBlank()) { "服务器地址未初始化" }

            if (retrofit == null || currentUrl != domain) {
                currentUrl = domain
                retrofit = Retrofit.Builder()
                    .baseUrl(domain.trimEnd('/') + "/")
                    .client(
                        OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(90, TimeUnit.SECONDS)
                            .writeTimeout(90, TimeUnit.SECONDS)
                            .addInterceptor { chain ->
                                val builder = chain.request().newBuilder()
                                val token = UserStore.getToken()

                                if (token.isNotBlank()) {
                                    val timestamp = (System.currentTimeMillis() / 1000).toString()
                                    val nonce = UUID.randomUUID().toString().replace("-", "")

                                    builder
                                        .header("X-Token", token)
                                        .header("X-Time", timestamp)
                                        .header("X-Nonce", nonce)
                                        .header("X-Sign", md5(token + timestamp + nonce + APP_SECRET))
                                }

                                chain.proceed(builder.build())
                            }
                            .build()
                    )
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            return requireNotNull(retrofit).create(ApiService::class.java)
        }

    fun reset() {
        retrofit = null
        currentUrl = ""
    }

    private fun md5(value: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(value.toByteArray())
            .joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
    }
}
