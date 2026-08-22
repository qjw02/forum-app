package com.qjw.forum

import com.google.gson.Gson
import okhttp3.ResponseBody

/**
 * 私信接口可能会被服务器插件或 PHP 警告污染。这里先读取原始内容，
 * 再解析 JSON；失败时提取 Discuz 数据库错误正文，避免只显示 HTML 页头。
 */
fun parsePrivateMessageResponse(body: ResponseBody): BaseResponse {
    val raw = body.string().trim()
    if (raw.isBlank()) {
        throw IllegalStateException("服务器未返回内容，请确认已上传 message_send_v3.php")
    }

    return try {
        Gson().fromJson(raw, BaseResponse::class.java)
            ?: throw IllegalStateException("服务器返回为空")
    } catch (_: Exception) {
        val readable = raw
            .replace(Regex("(?is)<script.*?</script>|<style.*?</style>"), " ")
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val discuzError = readable.indexOf("Discuz! Database Error", ignoreCase = true)
        val detail = when {
            discuzError >= 0 -> readable.substring(discuzError).take(900)
            else -> readable.take(700)
        }
        throw IllegalStateException("服务器返回异常：$detail")
    }
}
