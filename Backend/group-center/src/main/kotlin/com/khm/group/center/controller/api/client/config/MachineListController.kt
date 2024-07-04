package com.khm.group.center.controller.api.client.config

import com.khm.group.center.datatype.config.MachineBaseConfig
import com.khm.group.center.datatype.config.MachineConfig
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class MachineListController {

    @Operation(summary = "客户端获取服务器列表")
    @RequestMapping("/api/client/config/user_list", method = [RequestMethod.GET])
    fun machineBaseList(): List<MachineBaseConfig> {
        val machineBaseList = mutableListOf<MachineBaseConfig>()

        for (machineInfo in MachineConfig.machineList) {
            val machineBaseObject = machineInfo as MachineBaseConfig
            machineBaseList.add(machineBaseObject)
        }

        return machineBaseList
    }

}
