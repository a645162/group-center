package com.khm.group.center.datatype.response

/**
 * 用户信息响应类 - 仅包含用户名和英文名
 * 用于公开接口，不包含敏感信息
 */
data class UserInfoResponse(
    val name: String,
    val nameEng: String
)