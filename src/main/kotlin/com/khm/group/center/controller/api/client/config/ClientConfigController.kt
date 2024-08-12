package com.khm.group.center.controller.api.client.config

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.datatype.config.MachineBaseConfig
import com.khm.group.center.datatype.config.MachineConfig
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ClientConfigController {
    @Operation(summary = "客户端获取环境变量列表")
    @RequestMapping("/api/client/config/env_list", method = [RequestMethod.GET])
    fun getEnvList(): HashMap<String, String> {
        val result = HashMap<String, String>()

        val path = ConfigEnvironment.CLIENT_ENV_CONFIG_PATH
        val envList =
            ConfigEnvironment.readEnvFile(
                fileEnvListPath = path,
                includeClassName = false
            )
        result.putAll(envList)

        return result
    }

    @Operation(summary = "客户端获取服务器列表")
    @RequestMapping("/api/client/config/machine_list", method = [RequestMethod.GET])
    fun getMachineBaseList(): List<MachineBaseConfig> {
        val machineBaseList = mutableListOf<MachineBaseConfig>()

        for (machineInfo in MachineConfig.machineList) {
            val machineBaseObject: MachineBaseConfig =
                (machineInfo as MachineBaseConfig).copy()
            machineBaseList.add(machineBaseObject)
        }

        return machineBaseList
    }

    @Operation(summary = "客户端获取环境用户列表")
    @RequestMapping("/api/client/config/user_list", method = [RequestMethod.GET])
    fun getUserList(): List<GroupUserConfig> {
        return GroupUserConfig.userList
    }
}
