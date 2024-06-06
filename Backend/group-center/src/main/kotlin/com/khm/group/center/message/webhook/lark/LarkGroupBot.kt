package com.khm.group.center.message.webhook.lark

import com.alibaba.fastjson2.JSON
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LarkGroupBot(botId: String, var botKey: String = "") {
    var webhookUrl: String = "https://open.feishu.cn/open-apis/bot/v2/hook/"

    init {
        if (botId.startsWith(webhookUrl)) {
            webhookUrl = botId
        } else {
            webhookUrl += botId
        }
    }

    data class LarkBotMessage(
        val timestamp: String,
        val sign: String,
        val msg_type: String,
        val content: Content
    )

    private fun LarkBotMessage.toRequestBody(): RequestBody {
        return JSON.toJSONString(this)
            .toRequestBody("application/json".toMediaType())
    }

    data class Content(
        val text: String
    )

    fun sendText(content: String) {
        val finalContent = content.trim()

        val timestamp = LarkGroupBotKey.getLarkTimestamp()
        try {
            val secret = LarkGroupBotKey.larkBotSign(botKey, timestamp)
            val message = LarkBotMessage(
                timestamp.toString(),
                secret,
                "text",
                Content(finalContent)
            )
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(webhookUrl)
                .post(message.toRequestBody())
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                println(response.body?.string())
            }
        } catch (e: Exception) {
            println("Error: $e")
            return
        }
    }

    companion object {
        fun getAtUserHtml(userId: String): String {
            val finalUserId = userId.trim()
            if (finalUserId.isEmpty()) {
                return ""
            }
            return "<at user_id=\"$finalUserId\"></at>"
        }
    }
}
