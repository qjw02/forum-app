package com.qjw.forum

object ForumCache {
    private const val TTL_MILLIS = 30 * 60 * 1000L

    private var forums: List<ForumItem>? = null
    private var updatedAt: Long = 0L

    fun get(): List<ForumItem>? {
        return forums?.takeIf {
            System.currentTimeMillis() - updatedAt < TTL_MILLIS
        }
    }

    fun save(value: List<ForumItem>) {
        forums = value
        updatedAt = System.currentTimeMillis()
    }

    fun clear() {
        forums = null
        updatedAt = 0L
    }
}
