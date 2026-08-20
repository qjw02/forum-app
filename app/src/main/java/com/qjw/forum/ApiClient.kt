package com.qjw.forum

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
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
                            .addInterceptor { chain ->
                                val request = chain.request()
                                    .newBuilder()
                                    .apply {
                                        UserStore.getToken()
                                            .takeIf { it.isNotBlank() }
                                            ?.let { header("X-Token", it) }
                                    }
                                    .build()
                                chain.proceed(request)
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
}
