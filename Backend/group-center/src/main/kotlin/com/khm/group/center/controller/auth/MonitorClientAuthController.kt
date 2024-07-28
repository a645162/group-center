package com.khm.group.center.controller.auth

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.response.AuthResponse
import com.khm.group.center.security.password.MD5
import com.khm.group.center.security.program.ClientAccessKey
import com.khm.group.center.security.program.ClientIpWhiteList
import com.khm.group.center.spring.utils.Ip
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitorClientAuthController {

    @Operation(summary = "认证GPU监控客户端")
    @RequestMapping("/auth/client/auth", method = [RequestMethod.GET])
    fun auth(userName: String, password: String): AuthResponse {
        val ipAddress = Ip.getControllerRemoteIp()
        val machineConfig = MachineConfig.getMachineByNameEng(userName)
        val response = AuthResponse()
        response.ipAddress = ipAddress

        if (machineConfig == null) {
            response.isAuthenticated = false
            response.isSucceed = false
            response.haveError = true
            response.result = "未找到对应的机器配置"
            return response
        }

        val correctPassword = MD5.getMd5Hash(machineConfig.password).lowercase()
        if (
            correctPassword != password.lowercase()
            && machineConfig.password != password
        ) {
            response.isAuthenticated = false
            response.isSucceed = false
            response.haveError = true
            response.result = "密码错误"
            return response
        }

        ClientIpWhiteList.addIpToWhiteList(ipAddress)

        val clientAccessKey = ClientAccessKey()
        clientAccessKey.nameEng = userName
        clientAccessKey.ipAddress = ipAddress

        response.isAuthenticated = true
        response.isSucceed = true
        response.haveError = false
        response.result = "认证成功"
        response.accessKey = clientAccessKey.generateAccessKey()

        return response
    }

}
