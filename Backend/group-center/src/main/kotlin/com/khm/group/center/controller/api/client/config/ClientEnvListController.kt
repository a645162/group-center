package com.khm.group.center.controller.api.client.config

import com.khm.group.center.config.env.ConfigEnvironment
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ClientEnvListController {
    @Operation(summary = "客户端获取环境变量列表")
    @RequestMapping("/api/client/config/env_list", method = [RequestMethod.GET])
    fun envList(): HashMap<String, String> {
        val result = HashMap<String, String>()

        val path = ConfigEnvironment.CLIENT_ENV_CONFIG_PATH
        val envList = ConfigEnvironment.readEnvFile(path)
        result.putAll(envList)

        return result
    }
}
