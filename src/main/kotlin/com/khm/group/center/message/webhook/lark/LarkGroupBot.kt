package com.khm.group.center.message.webhook.lark

import com.alibaba.fastjson2.JSON
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.feature.SilentModeConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Slf4jKt
class LarkGroupBot(val botId: String, var botKey: String = "") {
    private var webhookUrl: String = "https://open.feishu.cn/open-apis/bot/v2/hook/"

    var isEnable: Boolean = true

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

    fun isValid(): Boolean {
        return isEnable && botId.isNotEmpty() && botKey.isNotEmpty()
    }

    fun sendText(content: String, atAll: Boolean = false): Boolean {
        val finalContent = if (atAll) {
            "${content.trim()}\n<at user_id=\"all\">所有人</at>"
        } else {
            content.trim()
        }

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
                    logger.warn("Failed to send lark group bot text for $botId, response code: ${response.code}")
                    return false
                }
                logger.debug("Successfully sent lark group bot text for $botId")
            }

            return true
        } catch (e: Exception) {
            logger.error("Failed to send lark group bot text for $botId, error: ${e.message}", e)
            return false
        }
    }

    suspend fun sendTextWithSilentMode(
        text: String,
        silentModeConfig: SilentModeConfig?,
        atAll: Boolean = false
    ) {
        if (silentModeConfig != null) {
            logger.info("Try to async send lark group bot text with silent mode for $botId")
            while (silentModeConfig.isSilentMode()) {
                logger.debug("Silent mode active for $botId, waiting...")
                delay(ConfigEnvironment.SilentModeWaitTime)
            }
            sendText(text, atAll)
            logger.info("Sent lark group bot text with silent mode for $botId")
        } else {
            logger.info("Sent lark group bot text with silent mode for $botId")
            sendText(text, atAll)
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
