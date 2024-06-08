package com.khm.group.center.controller.api.client.config

import com.khm.group.center.datatype.config.GroupUserConfig
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class UserListController {
    @Operation(summary = "客户端获取环境用户列表")
    @RequestMapping("/api/client/config/user_list", method = [RequestMethod.GET])
    fun userList(): List<GroupUserConfig> {
        return GroupUserConfig.userList
    }
}
