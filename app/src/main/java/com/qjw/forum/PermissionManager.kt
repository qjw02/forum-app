package com.qjw.forum

import com.google.gson.Gson
import com.google.gson.JsonParser

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
                    errorMessage = "权限接口暂时不可用，请稍后重试"
                )
            } else {
                val jsonText = raw.substring(start, end + 1)
                try {
                    val json = JsonParser.parseString(jsonText).asJsonObject
                    val code = json.get("code")?.asInt ?: -1
                    val message = json.get("message")?.asString

                    // 失败响应的 data 可能是 []，不能按成功对象解析；
                    // 直接保留服务器给出的中文原因即可。
                    if (code != 0) {
                        PermissionCheckResult(
                            response = PermissionResponse(code, message, null)
                        )
                    } else {
                        PermissionCheckResult(
                            response = Gson().fromJson(
                                jsonText,
                                PermissionResponse::class.java
                            )
                        )
                    }
                } catch (_: Exception) {
                    PermissionCheckResult(
                        errorMessage = "权限接口返回异常，请稍后重试"
                    )
                }
            }
        } catch (_: Exception) {
            PermissionCheckResult(
                errorMessage = "权限接口连接失败，请检查网络后重试"
            )
        }
    }

    suspend fun check(fid: String): Boolean {
        val result = request(fid).response
        return result?.code == 0 && result.data?.allow == true
    }
}
