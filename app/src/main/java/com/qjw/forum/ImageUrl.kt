package com.qjw.forum

import android.net.Uri

/** 将论坛附件图片转换成同一动态域名下的缓存缩略图。 */
fun appThumbnailUrl(source: String?, width: Int): String? {
    val raw = source?.trim().orEmpty()
    if (raw.isEmpty()) return source

    val domain = DomainManager.getDomain().trim().trimEnd('/')
    if (domain.isEmpty()) return source

    val attachmentPath = when {
        raw.startsWith("data/attachment/") -> raw
        raw.startsWith("/data/attachment/") -> raw.removePrefix("/")
        raw.startsWith(domain + "/") -> raw.removePrefix(domain).trimStart('/')
        else -> {
            val marker = "/data/attachment/"
            val markerIndex = raw.indexOf(marker)
            if (markerIndex < 0) return source
            raw.substring(markerIndex + 1)
        }
    }

    if (!attachmentPath.startsWith("data/attachment/")) return source

    val safeWidth = width.coerceIn(120, 1280)
    return domain + "/api/user/thumb.php?path=" + Uri.encode(attachmentPath) + "&w=" + safeWidth
}
