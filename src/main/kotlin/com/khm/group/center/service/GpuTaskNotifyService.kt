package com.khm.group.center.service

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.db.model.subscription.ProjectSubscriptionModel
import com.khm.group.center.message.webhook.lark.LarkBot
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * GPU任务通知服务
 * 负责在任务完成时发送飞书个人bot消息
 */
@Service
class GpuTaskNotifyService(
    private val projectSubscriptionService: ProjectSubscriptionService
) {

    /**
     * 异步处理任务完成通知
     */
    @Async
    fun notifyTaskCompletionAsync(projectId: String, taskId: String, taskName: String): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            try {
                notifyTaskCompletion(projectId, taskId, taskName)
                true
            } catch (e: Exception) {
                logger.error("异步通知任务完成失败: ${e.message}", e)
                false
            }
        }
    }

    /**
     * 处理任务完成通知
     */
    fun notifyTaskCompletion(projectId: String, taskId: String, taskName: String): Boolean {
        try {
            logger.info("开始处理任务完成通知: 项目 $projectId, 任务 $taskId, 任务名 $taskName")

            // 1. 首先检查是否有通过任务ID订阅的用户
            val taskSubscription = projectSubscriptionService.getTaskSubscribers(taskId)
            if (taskSubscription != null) {
                logger.info("找到任务ID订阅: 用户 ${taskSubscription.userNameEng}")
                sendTaskCompletionMessage(taskSubscription, projectId, taskId, taskName)
                // 标记订阅为已完成
                projectSubscriptionService.markSubscriptionAsCompleted(taskSubscription)
                return true
            }

            // 2. 如果没有任务ID订阅，检查项目订阅
            val projectSubscriptions = projectSubscriptionService.getProjectSubscribersFromDatabase(projectId)
            if (projectSubscriptions.isNotEmpty()) {
                logger.info("找到 ${projectSubscriptions.size} 个项目订阅者")
                
                var successCount = 0
                projectSubscriptions.forEach { subscription ->
                    try {
                        sendTaskCompletionMessage(subscription, projectId, taskId, taskName)
                        // 标记订阅为已完成
                        if (projectSubscriptionService.markSubscriptionAsCompleted(subscription)) {
                            successCount++
                        }
                    } catch (e: Exception) {
                        logger.error("发送任务完成消息失败: 用户 ${subscription.userNameEng}, 错误: ${e.message}")
                    }
                }
                
                logger.info("任务完成通知处理完成: 成功发送 $successCount/${projectSubscriptions.size} 条消息")
                return successCount > 0
            } else {
                logger.info("项目 $projectId 没有订阅者，无需发送通知")
                return false
            }
        } catch (e: Exception) {
            logger.error("处理任务完成通知失败: ${e.message}", e)
            return false
        }
    }

    /**
     * 发送任务完成消息
     */
    private fun sendTaskCompletionMessage(
        subscription: ProjectSubscriptionModel,
        projectId: String,
        taskId: String,
        taskName: String
    ) {
        try {
            val userNameEng = subscription.userNameEng
            val message = buildTaskCompletionMessage(projectId, taskId, taskName, subscription.userName)
            
            // 获取用户配置
            val userConfig = GroupUserConfig.getUserByNameEng(userNameEng)
            if (userConfig == null) {
                logger.warn("无法找到用户配置: $userNameEng")
                return
            }
            
            // 检查用户是否启用了飞书个人bot
            if (!userConfig.webhook.lark.enable) {
                logger.info("用户 $userNameEng 未启用飞书个人bot，跳过发送通知")
                return
            }
            
            // 检查飞书配置是否有效
            if (!LarkBot.isAppIdSecretValid()) {
                logger.warn("飞书bot配置无效，无法发送消息给用户 $userNameEng")
                return
            }
            
            // 创建LarkBot实例并发送消息
            val larkBot = LarkBot(userConfig.webhook.lark.userId)
            val success = larkBot.sendText(message)
            
            if (success) {
                logger.info("成功发送任务完成通知给用户 $userNameEng: 项目 $projectId, 任务 $taskId")
            } else {
                logger.warn("发送任务完成通知给用户 $userNameEng 失败")
            }
        } catch (e: Exception) {
            logger.error("发送任务完成消息给用户 ${subscription.userNameEng} 失败: ${e.message}", e)
            throw e
        }
    }

    /**
     * 构建任务完成消息内容
     */
    private fun buildTaskCompletionMessage(
        projectId: String,
        taskId: String,
        taskName: String,
        userName: String
    ): String {
        return """
            🎉 任务完成通知 🎉
            
            亲爱的 ${userName}，
            
            您订阅的项目已完成！
            
            📋 项目信息：
            • 项目ID：$projectId
            • 任务ID：$taskId
            • 任务名称：$taskName
            
            ⏰ 完成时间：${java.time.LocalDateTime.now()}
            
            感谢您的关注！
            
            ---
            GPU任务监控系统
        """.trimIndent()
    }

    /**
     * 检查项目是否有订阅者（用于任务接收时快速判断是否需要处理）
     */
    fun hasSubscribersForProject(projectId: String): Boolean {
        return projectSubscriptionService.hasSubscribers(projectId)
    }

    /**
     * 检查任务是否有订阅者（通过任务ID）
     */
    fun hasSubscribersForTask(taskId: String): Boolean {
        return projectSubscriptionService.getTaskSubscribers(taskId) != null
    }
}