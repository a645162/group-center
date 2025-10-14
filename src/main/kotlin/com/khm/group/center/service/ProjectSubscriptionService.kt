package com.khm.group.center.service

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.response.ProjectSubscriptionResponse
import com.khm.group.center.db.mapper.subscription.ProjectSubscriptionMapper
import com.khm.group.center.db.model.subscription.ProjectSubscriptionModel
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import jakarta.annotation.PostConstruct

/**
 * 项目订阅服务
 * 管理用户对项目的订阅关系，并在项目完成时推送通知
 * 支持数据库存储 + 内存缓存的双重模式
 */
@Service
class ProjectSubscriptionService(
    private val projectSubscriptionMapper: ProjectSubscriptionMapper
) {

    // 内存缓存：存储待处理的订阅关系（项目ID -> 订阅的用户英文名列表）
    private val pendingSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()

    /**
     * 根据用户名查找用户英文名（优先匹配英文名，没匹配到再匹配中文名）
     */
    private fun findUserNameEng(userName: String): String? {
        // 优先匹配英文名
        val userByEng = GroupUserConfig.getUserByNameEng(userName)
        if (userByEng != null) {
            return userByEng.nameEng
        }
        
        // 如果英文名没匹配到，匹配中文名
        val userByName = GroupUserConfig.getUserByName(userName)
        if (userByName != null) {
            return userByName.nameEng
        }
        
        return null
    }

    /**
     * 程序启动时从数据库加载所有待处理的订阅到内存缓存
     */
    @PostConstruct
    fun loadPendingSubscriptionsFromDatabase() {
        try {
            val pendingSubscriptionsList = projectSubscriptionMapper.findAllPending()
            pendingSubscriptionsList.forEach { subscription ->
                val projectId = subscription.projectId
                val userNameEng = subscription.userNameEng
                
                val subscribers = pendingSubscriptions.getOrPut(projectId) { mutableSetOf() }
                subscribers.add(userNameEng)
            }
            
            logger.info("Loaded ${pendingSubscriptionsList.size} pending subscriptions from database")
        } catch (e: Exception) {
            logger.error("Failed to load pending subscriptions from database: ${e.message}", e)
        }
    }

    /**
     * 用户订阅项目
     */
    fun subscribeProject(projectId: Long, userName: String): ProjectSubscriptionResponse {
        return subscribeProject(projectId.toString(), userName)
    }

    /**
     * 用户订阅项目（支持字符串项目ID）
     */
    fun subscribeProject(projectId: String, userName: String): ProjectSubscriptionResponse {
        try {
            // 查找用户英文名
            val userNameEng = findUserNameEng(userName)
            if (userNameEng == null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户不存在: $userName"
                )
            }

            // 检查是否已订阅（数据库检查）
            val existingSubscription = projectSubscriptionMapper.findPendingByProjectIdAndUserNameEng(projectId, userNameEng)
            if (existingSubscription != null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户 $userName 已经订阅了项目 $projectId"
                )
            }

            // 创建新的订阅记录
            val user = GroupUserConfig.getUserByNameEng(userNameEng) ?: GroupUserConfig.getUserByName(userName)
            val subscription = ProjectSubscriptionModel.createPending(
                projectId = projectId,
                userNameEng = userNameEng,
                userName = user?.name ?: userName
            )

            // 保存到数据库
            val result = projectSubscriptionMapper.insert(subscription)
            if (result > 0) {
                // 添加到内存缓存
                val subscribers = pendingSubscriptions.getOrPut(projectId) { mutableSetOf() }
                subscribers.add(userNameEng)
                
                logger.info("用户 $userName ($userNameEng) 订阅了项目 $projectId (ID: ${subscription.id})")
                
                return ProjectSubscriptionResponse(
                    success = true,
                    message = "订阅成功",
                    projectId = projectId.toLongOrNull() ?: 0,
                    userName = userName,
                    userNameEng = userNameEng
                )
            } else {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "订阅失败：数据库保存失败"
                )
            }
        } catch (e: Exception) {
            logger.error("订阅项目失败: ${e.message}", e)
            return ProjectSubscriptionResponse(
                success = false,
                message = "订阅失败: ${e.message}"
            )
        }
    }

    /**
     * 用户订阅项目（带任务ID）
     */
    fun subscribeProjectWithTaskId(projectId: String, userName: String, taskId: String): ProjectSubscriptionResponse {
        try {
            // 查找用户英文名
            val userNameEng = findUserNameEng(userName)
            if (userNameEng == null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户不存在: $userName"
                )
            }

            // 检查是否已订阅（数据库检查）
            val existingSubscription = projectSubscriptionMapper.findPendingByProjectIdAndUserNameEng(projectId, userNameEng)
            if (existingSubscription != null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户 $userName 已经订阅了项目 $projectId"
                )
            }

            // 创建新的订阅记录（带任务ID）
            val user = GroupUserConfig.getUserByNameEng(userNameEng) ?: GroupUserConfig.getUserByName(userName)
            val subscription = ProjectSubscriptionModel.createPendingWithTaskId(
                projectId = projectId,
                userNameEng = userNameEng,
                userName = user?.name ?: userName,
                taskId = taskId
            )

            // 保存到数据库
            val result = projectSubscriptionMapper.insert(subscription)
            if (result > 0) {
                // 添加到内存缓存
                val subscribers = pendingSubscriptions.getOrPut(projectId) { mutableSetOf() }
                subscribers.add(userNameEng)
                
                logger.info("用户 $userName ($userNameEng) 订阅了项目 $projectId (任务ID: $taskId, 订阅ID: ${subscription.id})")
                
                return ProjectSubscriptionResponse(
                    success = true,
                    message = "订阅成功",
                    projectId = projectId.toLongOrNull() ?: 0,
                    userName = userName,
                    userNameEng = userNameEng
                )
            } else {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "订阅失败：数据库保存失败"
                )
            }
        } catch (e: Exception) {
            logger.error("订阅项目失败: ${e.message}", e)
            return ProjectSubscriptionResponse(
                success = false,
                message = "订阅失败: ${e.message}"
            )
        }
    }

    /**
     * 用户取消订阅项目
     */
    fun unsubscribeProject(projectId: Long, userName: String): ProjectSubscriptionResponse {
        return unsubscribeProject(projectId.toString(), userName)
    }

    /**
     * 用户取消订阅项目（支持字符串项目ID）
     */
    fun unsubscribeProject(projectId: String, userName: String): ProjectSubscriptionResponse {
        try {
            // 查找用户英文名
            val userNameEng = findUserNameEng(userName)
            if (userNameEng == null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户不存在: $userName"
                )
            }

            // 从数据库删除订阅记录
            val deletedCount = projectSubscriptionMapper.deletePendingByProjectIdAndUserNameEng(projectId, userNameEng)
            
            // 从内存缓存中移除
            val subscribers = pendingSubscriptions[projectId]
            if (subscribers != null) {
                subscribers.remove(userNameEng)
                // 如果项目没有订阅者了，移除项目
                if (subscribers.isEmpty()) {
                    pendingSubscriptions.remove(projectId)
                }
            }

            if (deletedCount > 0) {
                logger.info("用户 $userName ($userNameEng) 取消订阅项目 $projectId")
                return ProjectSubscriptionResponse(
                    success = true,
                    message = "取消订阅成功",
                    projectId = projectId.toLongOrNull() ?: 0,
                    userName = userName,
                    userNameEng = userNameEng
                )
            } else {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "取消订阅失败：未找到订阅记录"
                )
            }
        } catch (e: Exception) {
            logger.error("取消订阅项目失败: ${e.message}", e)
            return ProjectSubscriptionResponse(
                success = false,
                message = "取消订阅失败: ${e.message}"
            )
        }
    }

    /**
     * 获取用户订阅的项目列表
     */
    fun getUserSubscriptions(userName: String): Set<String> {
        // 查找用户英文名
        val userNameEng = findUserNameEng(userName)
        if (userNameEng == null) {
            return emptySet()
        }

        return pendingSubscriptions.entries
            .filter { (_, subscribers) -> subscribers.contains(userNameEng) }
            .map { (projectId, _) -> projectId }
            .toSet()
    }

    /**
     * 获取项目所有订阅者（用户英文名列表）
     */
    fun getProjectSubscribers(projectId: String): Set<String> {
        return pendingSubscriptions[projectId]?.toSet() ?: emptySet()
    }

    /**
     * 获取项目所有订阅者（用户英文名列表）- 从数据库查询
     */
    fun getProjectSubscribersFromDatabase(projectId: String): List<ProjectSubscriptionModel> {
        return projectSubscriptionMapper.findByProjectIdAndStatus(projectId, "pending")
    }

    /**
     * 获取任务相关的订阅者
     */
    fun getTaskSubscribers(taskId: String): ProjectSubscriptionModel? {
        return projectSubscriptionMapper.findPendingByTaskId(taskId)
    }

    /**
     * 标记订阅为已完成
     */
    fun markSubscriptionAsCompleted(subscription: ProjectSubscriptionModel): Boolean {
        try {
            subscription.markAsCompleted()
            val result = projectSubscriptionMapper.updateById(subscription)
            
            if (result > 0) {
                // 从内存缓存中移除
                val subscribers = pendingSubscriptions[subscription.projectId]
                if (subscribers != null) {
                    subscribers.remove(subscription.userNameEng)
                    if (subscribers.isEmpty()) {
                        pendingSubscriptions.remove(subscription.projectId)
                    }
                }
                logger.info("标记订阅为已完成: 项目 ${subscription.projectId}, 用户 ${subscription.userNameEng}")
                return true
            }
            return false
        } catch (e: Exception) {
            logger.error("标记订阅为已完成失败: ${e.message}", e)
            return false
        }
    }

    /**
     * 检查项目是否有订阅者
     */
    fun hasSubscribers(projectId: String): Boolean {
        return pendingSubscriptions[projectId]?.isNotEmpty() ?: false
    }

    /**
     * 检查用户是否已订阅项目
     */
    fun isUserSubscribed(projectId: String, userName: String): Boolean {
        val userNameEng = findUserNameEng(userName) ?: return false
        return pendingSubscriptions[projectId]?.contains(userNameEng) ?: false
    }
}