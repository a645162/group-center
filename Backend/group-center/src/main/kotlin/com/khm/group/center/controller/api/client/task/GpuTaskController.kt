package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.config.MachineConfig
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

import com.khm.group.center.datatype.receive.GpuTaskInfo
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.mapper.GpuTaskInfoMapper
import org.springframework.beans.factory.annotation.Autowired


@RestController
class GpuTaskController {

    @Autowired
    lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    @Operation(summary = "GPU任务变动")
    @RequestMapping("/api/client/gpu_task/info", method = [RequestMethod.POST])
    fun gpuTaskInfo(@RequestBody gpuTaskInfo: GpuTaskInfo): ClientResponse {
        gpuTaskInfoMapper.insert(gpuTaskInfo)

        // Notify
        val machineConfig = MachineConfig.getMachineByNameEng(gpuTaskInfo.serverNameEng)

        val gpuTaskNotify = GpuTaskNotify(
            gpuTaskInfo = gpuTaskInfo,
            machineConfig = machineConfig
        )

        if (gpuTaskInfo.multiDeviceLocalRank < 1) {
            gpuTaskNotify.sendTaskMessage()
        }

        val responseObj = ClientResponse()
        responseObj.result = "success"
        responseObj.isSucceed = true
        responseObj.isAuthenticated = true
        return responseObj
    }

}
