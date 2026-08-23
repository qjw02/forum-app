package com.qjw.forum

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object AppAnalytics {
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    fun screen(name: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "QianJiaoApp")
        })
    }

    fun action(name: String, value: String? = null) {
        analytics?.logEvent(name, Bundle().apply {
            value?.let { putString("value", it.take(100)) }
        })
    }
}

data class AppUpdateInfo(
    val versionName: String,
    val downloadUrl: String,
    val message: String,
    val force: Boolean
)

object AppUpdateManager {
    private const val KEY_MIN_VERSION_CODE = "min_version_code"
    private const val KEY_LATEST_VERSION_NAME = "latest_version_name"
    private const val KEY_DOWNLOAD_URL = "download_url"
    private const val KEY_UPDATE_MESSAGE = "update_message"
    private const val KEY_FORCE_UPDATE = "force_update"

    fun check(onResult: (AppUpdateInfo?) -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(6 * 60 * 60)
                .build()
        )
        remoteConfig.setDefaultsAsync(
            mapOf(
                KEY_MIN_VERSION_CODE to 1L,
                KEY_LATEST_VERSION_NAME to BuildConfig.VERSION_NAME,
                KEY_DOWNLOAD_URL to "",
                KEY_UPDATE_MESSAGE to "发现新版本，建议立即更新。",
                KEY_FORCE_UPDATE to false
            )
        )
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            val minVersion = remoteConfig.getLong(KEY_MIN_VERSION_CODE)
            val downloadUrl = remoteConfig.getString(KEY_DOWNLOAD_URL).trim()
            if (minVersion > BuildConfig.VERSION_CODE && downloadUrl.startsWith("http")) {
                onResult(
                    AppUpdateInfo(
                        versionName = remoteConfig.getString(KEY_LATEST_VERSION_NAME)
                            .ifBlank { "新版本" },
                        downloadUrl = downloadUrl,
                        message = remoteConfig.getString(KEY_UPDATE_MESSAGE)
                            .ifBlank { "发现新版本，建议立即更新。" },
                        force = remoteConfig.getBoolean(KEY_FORCE_UPDATE)
                    )
                )
            } else {
                onResult(null)
            }
        }
    }
}
