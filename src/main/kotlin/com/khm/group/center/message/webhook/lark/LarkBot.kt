package com.khm.group.center.message.webhook.lark

import com.alibaba.fastjson2.JSON
import java.util.UUID

import com.lark.oapi.Client
import com.lark.oapi.core.utils.Jsons
import com.lark.oapi.service.im.v1.model.CreateMessageReq
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.feature.SilentModeConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.delay

@Slf4jKt
class LarkBot(val userId: String) {
    data class Content(
        val text: String
    )

    fun sendText(text: String): Boolean {
        try {
            logger.info("Starting to send Lark message to user: $userId")
            logger.debug("Message content preview: ${text.take(100)}...")
            
            // 构建client
            val client = Client.newBuilder(
                ConfigEnvironment.LARK_BOT_APP_ID,
                ConfigEnvironment.LARK_BOT_APP_SECRET
            ).build()

            val content = Content(text)
            val jsonContent = JSON.toJSONString(content)

            // 创建请求对象
            val req = CreateMessageReq.newBuilder()
                .receiveIdType("user_id")
                .createMessageReqBody(
                    CreateMessageReqBody.newBuilder()
                        .receiveId(userId)
                        .msgType("text")
                        .content(jsonContent)
                        .uuid(UUID.randomUUID().toString())
                        .build()
                )
                .build()

            logger.debug("Lark API request prepared for user: $userId")

            // 发起请求
            val resp = client.im().message().create(req)

            // 处理服务端错误
            if (!resp.success()) {
                logger.error("Lark API call failed for user $userId: code=${resp.code}, msg=${resp.msg}, reqId=${resp.requestId}")
                return false
            }

            // 业务数据处理
            logger.debug("Lark API response: ${Jsons.DEFAULT.toJson(resp.data)}")
            logger.info("Successfully sent Lark message to user: $userId")
            return true
        } catch (e: Exception) {
            logger.error("Failed to send Lark message to user $userId: ${e.message}", e)
            return false
        }
    }

    suspend fun sendTextWithSilentMode(
        text: String,
        silentModeConfig: SilentModeConfig
    ): Boolean {
        logger.info("Starting async Lark message with silent mode for user: $userId")
        logger.debug("Silent mode config: $silentModeConfig")
        
        while (silentModeConfig.isSilentMode()) {
            logger.debug("Silent mode active for user $userId, waiting...")
            // Delay
            delay(ConfigEnvironment.SilentModeWaitTime)
        }
        
        logger.info("Silent mode ended, sending message to user: $userId")
        val isSuccess = sendText(text)
        logger.info("Async Lark message with silent mode completed for user $userId: success=$isSuccess")

        return isSuccess
    }

    companion object {
        fun isAppIdSecretValid(): Boolean {
            return (
                    ConfigEnvironment.LARK_BOT_APP_ID.isNotEmpty()
                            && ConfigEnvironment.LARK_BOT_APP_SECRET.isNotEmpty()
                    )
        }
    }
}
