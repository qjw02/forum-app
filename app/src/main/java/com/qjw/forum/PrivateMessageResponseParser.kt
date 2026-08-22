package com.qjw.forum

import com.google.gson.Gson
import okhttp3.ResponseBody

/**
 * 私信接口可能会被服务器插件或 PHP 警告污染。这里先读取原始内容，
 * 再解析 JSON；失败时把真正的服务器返回内容展示给用户，便于定位。
 */
fun parsePrivateMessageResponse(body: ResponseBody): BaseResponse {
    val raw = body.string().trim()
    if (raw.isBlank()) {
        throw IllegalStateException("服务器未返回内容，请确认已上传 message_send_v2.php")
    }

    return try {
        Gson().fromJson(raw, BaseResponse::class.java)
            ?: throw IllegalStateException("服务器返回为空")
    } catch (_: Exception) {
        val preview = raw.replace(Regex("\\s+"), " ").take(280)
        throw IllegalStateException("服务器返回异常：$preview")
    }
}
