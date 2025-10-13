package com.khm.group.center.service

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.response.ProjectSubscriptionResponse
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * 项目订阅服务
 * 管理用户对项目的订阅关系，并在项目完成时推送通知
 */
@Service
class ProjectSubscriptionService {

    // 存储项目订阅关系：项目名 -> 订阅的用户名列表
    private val projectSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()

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
     * 用户订阅项目
     */
    fun subscribeProject(projectId: Long, userName: String): ProjectSubscriptionResponse {
        try {
            // 查找用户英文名
            val userNameEng = findUserNameEng(userName)
            if (userNameEng == null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户不存在: $userName"
                )
            }

            // 检查是否已订阅
            val subscribers = projectSubscriptions.getOrPut(projectId.toString()) { mutableSetOf() }
            if (subscribers.contains(userNameEng)) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户 $userName 已经订阅了项目 $projectId"
                )
            }

            // 添加订阅关系
            subscribers.add(userNameEng)

            logger.info("用户 $userName ($userNameEng) 订阅了项目 $projectId")
            
            return ProjectSubscriptionResponse(
                success = true,
                message = "订阅成功",
                projectId = projectId,
                userName = userName,
                userNameEng = userNameEng
            )
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
        try {
            // 查找用户英文名
            val userNameEng = findUserNameEng(userName)
            if (userNameEng == null) {
                return ProjectSubscriptionResponse(
                    success = false,
                    message = "用户不存在: $userName"
                )
            }

            val subscribers = projectSubscriptions[projectId.toString()]
            if (subscribers != null) {
                subscribers.remove(userNameEng)
                // 如果项目没有订阅者了，移除项目
                if (subscribers.isEmpty()) {
                    projectSubscriptions.remove(projectId.toString())
                }
                logger.info("用户 $userName ($userNameEng) 取消订阅项目 $projectId")
            }

            return ProjectSubscriptionResponse(
                success = true,
                message = "取消订阅成功",
                projectId = projectId,
                userName = userName,
                userNameEng = userNameEng
            )
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

        return projectSubscriptions.entries
            .filter { (_, subscribers) -> subscribers.contains(userNameEng) }
            .map { (projectId, _) -> projectId }
            .toSet()
    }
}