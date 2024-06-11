package com.khm.group.center.message.webhook.lark

import com.alibaba.fastjson2.JSON
import java.util.UUID

import com.lark.oapi.Client
import com.lark.oapi.core.utils.Jsons
import com.lark.oapi.service.im.v1.model.CreateMessageReq
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.feature.SilentModeConfig
import kotlinx.coroutines.delay

class LarkBot(val userId: String) {
    data class Content(
        val text: String
    )

    fun sendText(text: String) {
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

        // 发起请求
        val resp = client.im().message().create(req)

        // 处理服务端错误
        if (!resp.success()) {
            println(String.format("code:%s,msg:%s,reqId:%s", resp.code, resp.msg, resp.requestId))
            return
        }

        // 业务数据处理
        println(Jsons.DEFAULT.toJson(resp.data))
    }

    suspend fun sendTextWithSilentMode(
        text: String,
        silentModeConfig: SilentModeConfig
    ) {
        while (silentModeConfig.isSilentMode()) {
            // Delay
            delay(ConfigEnvironment.SilentModeWaitTime)
        }
        sendText(text)
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
