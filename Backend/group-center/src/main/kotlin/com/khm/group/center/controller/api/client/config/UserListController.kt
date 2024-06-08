package com.khm.group.center.controller.api.client.config

import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.response.ClientResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class UserListController {

    @Operation(summary = "Test")
    @RequestMapping("/api/client/config/version", method = [RequestMethod.GET])
    fun version(): ClientResponse {
        val responseObj = ClientResponse()
        responseObj.result = "success"
        responseObj.isAuthenticated = true
        return responseObj
    }

    @RequestMapping("/api/client/config/user_list", method = [RequestMethod.GET])
    fun userList(): List<GroupUserConfig> {
        return GroupUserConfig.userList
    }
}
