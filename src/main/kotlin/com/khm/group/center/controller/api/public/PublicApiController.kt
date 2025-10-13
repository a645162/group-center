package com.khm.group.center.controller.api.public

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.datatype.response.ProjectCompletionNotification
import com.khm.group.center.datatype.response.ProjectSubscriptionRequest
import com.khm.group.center.datatype.response.ProjectSubscriptionResponse
import com.khm.group.center.datatype.response.UserInfoResponse
import com.khm.group.center.service.ProjectSubscriptionService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 公开API控制器
 * 提供无需认证的公开接口
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Public API", description = "公开API接口")
class PublicApiController {

    @Autowired
    private lateinit var projectSubscriptionService: ProjectSubscriptionService

    /**
     * 获取用户列表（公开接口）
     * 仅返回用户名和英文名，不包含敏感信息
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "获取所有用户的用户名和英文名列表")
    fun getUserList(): ClientResponse {
        try {
            val userList = GroupUserConfig.userList.map { user ->
                UserInfoResponse(
                    name = user.name,
                    nameEng = user.nameEng
                )
            }

            logger.info("公开接口返回用户列表，共 ${userList.size} 个用户")
            
            val result = ClientResponse()
            result.result = userList
            return result
        } catch (e: Exception) {
            logger.error("获取用户列表失败: ${e.message}", e)
            val result = ClientResponse()
            result.haveError = true
            result.isSucceed = false
            result.result = "获取用户列表失败: ${e.message}"
            return result
        }
    }

    /**
     * 订阅项目（公开接口）
     */
    @PostMapping("/projects/subscribe")
    @Operation(summary = "订阅项目", description = "用户订阅指定项目，项目完成时会收到通知")
    fun subscribeProject(@RequestBody request: ProjectSubscriptionRequest): ClientResponse {
        try {
            val result = projectSubscriptionService.subscribeProject(
                projectId = request.projectId,
                userName = request.userName
            )

            return if (result.success) {
                val response = ClientResponse()
                response.isSucceed = true
                response.haveError = false
                response.result = result
                response
            } else {
                val response = ClientResponse()
                response.isSucceed = false
                response.haveError = true
                response.result = result.message
                response
            }
        } catch (e: Exception) {
            logger.error("订阅项目失败: ${e.message}", e)
            val response = ClientResponse()
            response.isSucceed = false
            response.haveError = true
            response.result = "订阅项目失败: ${e.message}"
            return response
        }
    }

    /**
     * 取消订阅项目（公开接口）
     */
    @PostMapping("/projects/unsubscribe")
    @Operation(summary = "取消订阅项目", description = "用户取消订阅指定项目")
    fun unsubscribeProject(@RequestBody request: ProjectSubscriptionRequest): ClientResponse {
        try {
            val result = projectSubscriptionService.unsubscribeProject(
                projectId = request.projectId,
                userName = request.userName
            )

            return if (result.success) {
                val response = ClientResponse()
                response.isSucceed = true
                response.haveError = false
                response.result = result
                response
            } else {
                val response = ClientResponse()
                response.isSucceed = false
                response.haveError = true
                response.result = result.message
                response
            }
        } catch (e: Exception) {
            logger.error("取消订阅项目失败: ${e.message}", e)
            val response = ClientResponse()
            response.isSucceed = false
            response.haveError = true
            response.result = "取消订阅项目失败: ${e.message}"
            return response
        }
    }

    /**
     * 获取用户订阅的项目列表（公开接口）
     */
    @GetMapping("/projects/subscriptions")
    @Operation(summary = "获取用户订阅列表", description = "获取指定用户订阅的所有项目列表")
    fun getUserSubscriptions(userName: String): ClientResponse {
        try {
            val subscriptions = projectSubscriptionService.getUserSubscriptions(userName)

            val response = ClientResponse()
            response.isSucceed = true
            response.haveError = false
            response.result = mapOf(
                "userName" to userName,
                "subscriptions" to subscriptions,
                "count" to subscriptions.size
            )
            return response
        } catch (e: Exception) {
            logger.error("获取用户订阅列表失败: ${e.message}", e)
            val response = ClientResponse()
            response.isSucceed = false
            response.haveError = true
            response.result = "获取订阅列表失败: ${e.message}"
            return response
        }
    }

}