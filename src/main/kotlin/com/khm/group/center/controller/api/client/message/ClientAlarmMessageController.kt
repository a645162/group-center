package com.khm.group.center.controller.api.client.message

import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.service.BotPushService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * 客户端报警消息控制器
 * 提供客户端直接发送报警消息到报警群的功能
 */
@RestController
@RequestMapping("/api/client/alarm")
@Tag(name = "Client Alarm Message", description = "Client alarm message API for sending messages directly to alarm groups")
@Slf4jKt
class ClientAlarmMessageController {

    /**
     * 发送报警消息到报警群
     * @param alarmMessage 报警消息请求体
     * @return 操作结果
     */
    @Operation(summary = "发送报警消息到报警群")
    @RequestMapping(method = [RequestMethod.POST])
    fun sendAlarmMessage(@RequestBody alarmMessage: ClientAlarmMessage): ClientResponse {
        val responseObj = ClientResponse()
        
        try {
            logger.info("Received alarm message from client: ${alarmMessage.title}, urgent: ${alarmMessage.urgent}")
            
            // 验证消息内容
            if (alarmMessage.content.isBlank()) {
                responseObj.result = "error: 消息内容不能为空"
                responseObj.isSucceed = false
                responseObj.haveError = true
                logger.warn("Alarm message content is empty")
                return responseObj
            }
            
            // 使用BotPushService发送消息到报警群
            BotPushService.pushToAlarmGroup(
                message = formatAlarmMessage(alarmMessage),
                urgent = alarmMessage.urgent
            )
            
            responseObj.result = "success"
            responseObj.isSucceed = true
            responseObj.isAuthenticated = true
            logger.info("Alarm message sent successfully: ${alarmMessage.title}")
            
        } catch (e: Exception) {
            responseObj.result = "error: ${e.message}"
            responseObj.isSucceed = false
            responseObj.haveError = true
            logger.error("Failed to send alarm message: ${e.message}", e)
        }
        
        return responseObj
    }

    /**
     * 格式化报警消息
     * @param alarmMessage 原始报警消息
     * @return 格式化后的消息内容
     */
    private fun formatAlarmMessage(alarmMessage: ClientAlarmMessage): String {
        val title = alarmMessage.title.ifBlank { "🚨 客户端报警" }
        val content = alarmMessage.content.trim()
        val source = alarmMessage.source.ifBlank { "未知来源" }
        
        return buildString {
            append("$title\n")
            append("=".repeat(title.length))
            append("\n\n")
            append("📋 消息内容:\n")
            append("$content\n\n")
            append("📍 来源: $source")
            
            // 添加时间戳
            val currentTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            append("\n\n⏰ 时间: $currentTime")
            
            // 如果是紧急消息，添加紧急标记
            if (alarmMessage.urgent) {
                append("\n\n⚠️ 紧急消息")
            }
        }
    }
}

/**
 * 客户端报警消息数据类
 */
data class ClientAlarmMessage(
    /** 消息标题 */
    var title: String = "",
    /** 消息内容 */
    var content: String = "",
    /** 消息来源（机器名称等） */
    var source: String = "",
    /** 是否紧急消息 */
    var urgent: Boolean = false
)
