package com.khm.group.center.message.webhook.lark

import com.alibaba.fastjson2.JSON
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.feature.SilentModeConfig
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LarkGroupBot(val botId: String, var botKey: String = "") {
    private var webhookUrl: String = "https://open.feishu.cn/open-apis/bot/v2/hook/"

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

    fun sendText(content: String): Boolean {
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
                if (!response.isSuccessful) {
                    println("Failed to send lark group bot text for $botId")
                    return false
                }
                println(response.body.string())
            }

            return true
        } catch (e: Exception) {
            println("Failed to send lark group bot text for $botId")
            println("Error: $e")
            return false
        }
    }

    suspend fun sendTextWithSilentMode(
        text: String,
        silentModeConfig: SilentModeConfig
    ) {
        println("Try to async send lark group bot text with silent mode for $botId")
        while (silentModeConfig.isSilentMode()) {
            // Delay
            delay(ConfigEnvironment.SilentModeWaitTime)
        }
        sendText(text)
        println("Sent lark group bot text with silent mode for $botId")
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
