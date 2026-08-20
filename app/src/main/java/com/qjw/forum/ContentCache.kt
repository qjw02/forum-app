package com.qjw.forum

import android.content.Context
import com.google.gson.Gson

object ContentCache {
    private const val PREF_NAME = "qjw_content_cache"
    private const val KEY_HOME_VERSION = "home_version"
    private const val KEY_HOME_DATA = "home_data"

    private var context: Context? = null
    private val gson = Gson()

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun getHome(): CachedHome? {
        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return null
        val version = prefs.getString(KEY_HOME_VERSION, "").orEmpty()
        val data = prefs.getString(KEY_HOME_DATA, "").orEmpty()

        if (version.isBlank() || data.isBlank()) return null

        return runCatching {
            CachedHome(version, gson.fromJson(data, HomeData::class.java))
        }.getOrNull()
    }

    fun saveHome(version: String, data: HomeData) {
        if (version.isBlank()) return

        context
            ?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_HOME_VERSION, version)
            ?.putString(KEY_HOME_DATA, gson.toJson(data))
            ?.apply()
    }

    fun getForum(fid: String): CachedForum? {
        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return null
        val version = prefs.getString("forum_version_$fid", "").orEmpty()
        val data = prefs.getString("forum_data_$fid", "").orEmpty()

        if (version.isBlank() || data.isBlank()) return null

        return runCatching {
            CachedForum(version, gson.fromJson(data, ForumThreadData::class.java))
        }.getOrNull()
    }

    fun saveForum(fid: String, version: String, data: ForumThreadData) {
        if (version.isBlank()) return

        context
            ?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("forum_version_$fid", version)
            ?.putString("forum_data_$fid", gson.toJson(data))
            ?.apply()
    }
}

data class CachedHome(
    val version: String,
    val data: HomeData
)

data class CachedForum(
    val version: String,
    val data: ForumThreadData
)
