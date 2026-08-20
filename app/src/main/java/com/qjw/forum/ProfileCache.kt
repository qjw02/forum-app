package com.qjw.forum

import android.content.Context
import com.google.gson.Gson

object ProfileCache {
    private const val PREF_NAME = "qjw_profile_cache"
    private const val TTL_MILLIS = 5 * 60 * 1000L

    private var context: Context? = null
    private val gson = Gson()

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun get(uid: Int): ProfileData? {
        if (uid <= 0) return null

        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return null
        val updatedAt = prefs.getLong("updated_at_" + uid, 0L)
        val data = prefs.getString("data_" + uid, "").orEmpty()

        if (data.isBlank() || System.currentTimeMillis() - updatedAt >= TTL_MILLIS) {
            return null
        }

        return runCatching {
            gson.fromJson(data, ProfileData::class.java)
        }.getOrNull()
    }

    fun save(profile: ProfileData) {
        if (profile.uid <= 0) return

        context
            ?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putLong("updated_at_" + profile.uid, System.currentTimeMillis())
            ?.putString("data_" + profile.uid, gson.toJson(profile))
            ?.apply()
    }

    fun clear(uid: Int = UserStore.getUid()) {
        if (uid <= 0) return

        context
            ?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.remove("updated_at_" + uid)
            ?.remove("data_" + uid)
            ?.apply()
    }
}
