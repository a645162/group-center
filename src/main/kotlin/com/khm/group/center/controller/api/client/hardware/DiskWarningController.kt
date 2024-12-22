package com.khm.group.center.controller.api.client.hardware

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.hardware.HardDiskUserUsage
import com.khm.group.center.datatype.response.ClientResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class DiskWarningController {

    @RequestMapping("/api/client/hardware/disk_usage_user", method = [RequestMethod.POST])
    fun postGpuTaskInfo(@RequestBody hardDiskUserUsage: HardDiskUserUsage): ClientResponse {

        val machineConfig = MachineConfig.getMachineByNameEng(hardDiskUserUsage.serverNameEng)

        val diskWarningUserNotify = DiskWarningUserNotify(
            hardDiskUserUsage = hardDiskUserUsage,
            machineConfig = machineConfig
        )

        diskWarningUserNotify.sendUserMessage()

        val responseObj = ClientResponse()
        responseObj.result = "success"
        responseObj.isSucceed = true
        responseObj.isAuthenticated = true
        return responseObj
    }

}
