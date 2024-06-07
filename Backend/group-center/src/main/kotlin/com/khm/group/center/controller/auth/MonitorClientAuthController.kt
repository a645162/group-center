package com.khm.group.center.controller.auth

import com.khm.group.center.config.MachineConfigParser
import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.response.AuthResponse
import com.khm.group.center.security.program.ClientAccessKey
import com.khm.group.center.utils.program.ProgramInfo
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.function.ServerRequest

@RestController
class MonitorClientAuthController {

    @Operation(summary = "认证GPU监控客户端")
    @RequestMapping("/auth/client/auth", method = [RequestMethod.GET])
    fun auth(userName: String, password: String): AuthResponse {
        val machineConfig = MachineConfig.getMachineByNameEng(userName)
        val response = AuthResponse()

        if (machineConfig == null) {
            response.isAuthenticated = false
            response.isSucceed = false
            response.haveError = true
            response.result = "未找到对应的机器配置"
            return response
        }

        if (machineConfig.password != password) {
            response.isAuthenticated = false
            response.isSucceed = false
            response.haveError = true
            response.result = "密码错误"
            return response
        }

        val clientAccessKey = ClientAccessKey()
        clientAccessKey.nameEng = userName

        response.isAuthenticated = true
        response.isSucceed = true
        response.haveError = false
        response.result = "认证成功"
        response.accessKey = clientAccessKey.generateAccessKey()

        return response
    }

}
