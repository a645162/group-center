package com.khm.group.center.service

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.message.webhook.lark.LarkBot
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

/**
 * 统一推送服务
 * 提供飞书和企业微信的推送功能，包含重试机制
 */
@Service
@Slf4jKt
class UnifiedPushService {

    companion object {
        // 重试配置
        private const val MAX_RETRY_COUNT = 3
        private val RETRY_DELAYS = longArrayOf(1000L, 3000L, 5000L) // 1s, 3s, 5s
    }

    /**
     * 推送消息到飞书群组
     * @param botId 飞书群组bot ID
     * @param botKey 飞书群组bot密钥
     * @param content 消息内容
     * @param urgent 是否紧急消息
     * @return 推送是否成功
     */
    suspend fun pushToLarkGroup(
        botId: String,
        botKey: String,
        content: String,
        urgent: Boolean = false
    ): Boolean {
        return withRetry("LarkGroup[$botId]") { retryCount ->
            try {
                val larkBot = LarkGroupBot(botId, botKey)
                val success = larkBot.sendText(content, urgent)
                if (success) {
                    logger.info("Successfully pushed to Lark group: $botId, retry: $retryCount")
                } else {
                    logger.warn("Failed to push to Lark group: $botId, retry: $retryCount")
                }
                success
            } catch (e: Exception) {
                logger.error("Exception when pushing to Lark group $botId, retry: $retryCount, error: ${e.message}")
                false
            }
        }
    }

    /**
     * 推送消息到飞书个人
     * @param userId 飞书用户ID
     * @param content 消息内容
     * @return 推送是否成功
     */
    suspend fun pushToLarkUser(
        userId: String,
        content: String
    ): Boolean {
        return withRetry("LarkUser[$userId]") { retryCount ->
            try {
                if (!LarkBot.isAppIdSecretValid()) {
                    logger.warn("Lark bot config invalid, cannot send message to user: $userId")
                    return@withRetry false
                }

                val larkBot = LarkBot(userId)
                val success = larkBot.sendText(content)
                if (success) {
                    logger.info("Successfully pushed to Lark user: $userId, retry: $retryCount")
                } else {
                    logger.warn("Failed to push to Lark user: $userId, retry: $retryCount")
                }
                success
            } catch (e: Exception) {
                logger.error("Exception when pushing to Lark user $userId, retry: $retryCount, error: ${e.message}")
                false
            }
        }
    }

    /**
     * 推送消息到企业微信群组
     * @param botKey 企业微信群组bot密钥
     * @param content 消息内容
     * @param urgent 是否紧急消息
     * @return 推送是否成功
     */
    suspend fun pushToWeComGroup(
        botKey: String,
        content: String,
        urgent: Boolean = false
    ): Boolean {
        return withRetry("WeComGroup[$botKey]") { retryCount ->
            try {
                WeComGroupBot.directSendTextWithUrl(
                    botKey, content,
                    null, null
                )
                logger.info("Successfully pushed to WeCom group, retry: $retryCount")
                true
            } catch (e: Exception) {
                logger.error("Exception when pushing to WeCom group, retry: $retryCount, error: ${e.message}")
                false
            }
        }
    }

    /**
     * 推送消息到指定用户（自动选择飞书个人或群组）
     * @param userNameEng 用户英文名
     * @param content 消息内容
     * @param title 消息标题（可选）
     * @return 推送是否成功
     */
    suspend fun pushToUser(
        userNameEng: String,
        content: String,
        title: String? = null
    ): Boolean {
        val userConfig = GroupUserConfig.getUserByNameEng(userNameEng)
        if (userConfig == null) {
            logger.warn("User config not found: $userNameEng")
            return false
        }

        val fullContent = if (title != null) "[$title]\n$content" else content
        var success = false

        // 推送到飞书个人
        if (userConfig.webhook.lark.enable && userConfig.webhook.lark.userId.isNotEmpty()) {
            success = pushToLarkUser(userConfig.webhook.lark.userId, fullContent)
        }

        return success
    }

    /**
     * 推送消息到指定群组类型
     * @param groupType 群组类型 (alarm, shortterm, longterm)
     * @param content 消息内容
     * @param title 消息标题（可选）
     * @param urgent 是否紧急消息
     * @return 推送是否成功
     */
    suspend fun pushToGroupType(
        groupType: String,
        content: String,
        title: String? = null,
        urgent: Boolean = false
    ): Boolean {
        val botPushService = BotPushService()
        val groups = botPushService.getBotGroupsByType(groupType)
        
        if (groups.isEmpty()) {
            logger.warn("No valid bot groups found for type: $groupType")
            return false
        }

        val fullContent = if (title != null) "[$title]\n$content" else content
        var successCount = 0

        for (group in groups) {
            var groupSuccess = false

            // 推送到飞书群组
            if (group.larkGroupBotId.isNotBlank() && group.larkGroupBotKey.isNotBlank()) {
                groupSuccess = pushToLarkGroup(group.larkGroupBotId, group.larkGroupBotKey, fullContent, urgent)
            }

            // 推送到企业微信群组
            if (!groupSuccess && group.weComGroupBotKey.isNotBlank()) {
                groupSuccess = pushToWeComGroup(group.weComGroupBotKey, fullContent, urgent)
            }

            if (groupSuccess) {
                successCount++
                logger.info("Successfully pushed to $groupType group: ${group.name}")
            } else {
                logger.warn("Failed to push to $groupType group: ${group.name}")
            }
        }

        logger.info("Push to $groupType groups completed: $successCount/${groups.size} successful")
        return successCount > 0
    }

    /**
     * 重试机制实现
     */
    private suspend fun <T> withRetry(operation: String, block: suspend (retryCount: Int) -> T): T {
        var lastException: Exception? = null
        
        for (retryCount in 0..MAX_RETRY_COUNT) {
            try {
                val result = block(retryCount)
                if (result is Boolean && result) {
                    return result
                }
                
                if (retryCount < MAX_RETRY_COUNT) {
                    val delayMs = RETRY_DELAYS.getOrElse(retryCount) { RETRY_DELAYS.last() }
                    logger.info("Retry $operation after ${delayMs}ms, attempt: ${retryCount + 1}")
                    delay(delayMs)
                }
            } catch (e: Exception) {
                lastException = e
                if (retryCount < MAX_RETRY_COUNT) {
                    val delayMs = RETRY_DELAYS.getOrElse(retryCount) { RETRY_DELAYS.last() }
                    logger.warn("Exception in $operation, retry after ${delayMs}ms, attempt: ${retryCount + 1}, error: ${e.message}")
                    delay(delayMs)
                }
            }
        }
        
        logger.error("All retry attempts failed for $operation")
        throw lastException ?: RuntimeException("Operation failed after $MAX_RETRY_COUNT retries")
    }
}