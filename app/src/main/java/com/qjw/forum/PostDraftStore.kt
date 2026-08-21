package com.qjw.forum

import android.content.Context

data class PostDraft(
    val forumId: Int?,
    val subject: String,
    val message: String,
    val sellContact: Boolean,
    val contact: String,
    val price: String
)

object PostDraftStore {
    private const val PREF_NAME = "qjw_post_drafts"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun load(context: Context, key: String): PostDraft? {
        val pref = prefs(context)
        if (!pref.contains("${key}_subject")) return null

        return PostDraft(
            forumId = pref.getInt("${key}_forum_id", 0).takeIf { it > 0 },
            subject = pref.getString("${key}_subject", "") ?: "",
            message = pref.getString("${key}_message", "") ?: "",
            sellContact = pref.getBoolean("${key}_sell_contact", false),
            contact = pref.getString("${key}_contact", "") ?: "",
            price = pref.getString("${key}_price", "10") ?: "10"
        )
    }

    fun save(context: Context, key: String, draft: PostDraft) {
        prefs(context).edit()
            .putInt("${key}_forum_id", draft.forumId ?: 0)
            .putString("${key}_subject", draft.subject)
            .putString("${key}_message", draft.message)
            .putBoolean("${key}_sell_contact", draft.sellContact)
            .putString("${key}_contact", draft.contact)
            .putString("${key}_price", draft.price)
            .apply()
    }

    fun clear(context: Context, key: String) {
        prefs(context).edit()
            .remove("${key}_forum_id")
            .remove("${key}_subject")
            .remove("${key}_message")
            .remove("${key}_sell_contact")
            .remove("${key}_contact")
            .remove("${key}_price")
            .apply()
    }
}
