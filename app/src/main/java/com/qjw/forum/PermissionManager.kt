package com.qjw.forum

import com.google.gson.Gson

data class PermissionCheckResult(
    val response: PermissionResponse? = null,
    val errorMessage: String? = null
)

object PermissionManager {
    suspend fun request(fid: String): PermissionCheckResult {
        return try {
            val raw = ApiClient.api.checkPermission(
                fid = fid,
                uid = UserStore.getUid()
            ).string().trim()
            val start = raw.indexOf('{')
            val end = raw.lastIndexOf('}')
            if (start < 0 || end <= start) {
                PermissionCheckResult(
                    errorMessage = "权限接口返回异常：" + raw
                        .replace(Regex("<[^>]*>"), " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                        .take(160)
                )
            } else {
                PermissionCheckResult(
                    response = Gson().fromJson(
                        raw.substring(start, end + 1),
                        PermissionResponse::class.java
                    )
                )
            }
        } catch (e: Exception) {
            PermissionCheckResult(
                errorMessage = "权限接口连接失败：" + (e.message ?: "未知错误").take(120)
            )
        }
    }

    suspend fun check(fid: String): Boolean {
        val result = request(fid).response
        return result?.code == 0 && result.data?.allow == true
    }
}
