package com.khm.group.center.datatype.response

/**
 * 项目订阅请求
 */
data class ProjectSubscriptionRequest(
    val projectId: Long,
    val userName: String
)

/**
 * 项目订阅响应
 */
data class ProjectSubscriptionResponse(
    val success: Boolean,
    val message: String,
    val projectId: Long? = null,
    val userName: String? = null,
    val userNameEng: String? = null
)

/**
 * 项目完成通知请求
 */
data class ProjectCompletionNotification(
    val projectName: String,
    val message: String
)