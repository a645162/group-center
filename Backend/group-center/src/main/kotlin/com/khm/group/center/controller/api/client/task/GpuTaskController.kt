package com.khm.group.center.controller.api.client.task

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.datatype.config.MachineConfig
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

import com.khm.group.center.datatype.receive.GpuTaskInfo
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.mapper.client.GpuTaskInfoMapper
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import org.springframework.beans.factory.annotation.Autowired


@RestController
class GpuTaskController {

    @Autowired
    lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    private fun getGpuTaskInfoByTaskId(taskId: String): GpuTaskInfoModel? {
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()
        queryWrapper.eq("task_id", taskId)
        return gpuTaskInfoMapper.selectOne(queryWrapper)
    }

    private fun newGpuTaskInfo(gpuTaskInfo: GpuTaskInfo) {
        if (gpuTaskInfo.taskId.isEmpty()) {
            return
        }

        val gpuTaskInfoModel = GpuTaskInfoModel.fromGpuTaskInfo(gpuTaskInfo)

        getGpuTaskInfoByTaskId(gpuTaskInfoModel.taskId)?.let {
            gpuTaskInfoModel.id = it.id
            gpuTaskInfoMapper.updateById(gpuTaskInfoModel)
            return
        }

        gpuTaskInfoMapper.insert(gpuTaskInfoModel)
    }

    @Operation(summary = "GPU任务变动")
    @RequestMapping("/api/client/gpu_task/info", method = [RequestMethod.POST])
    fun postGpuTaskInfo(@RequestBody gpuTaskInfo: GpuTaskInfo): ClientResponse {
        newGpuTaskInfo(gpuTaskInfo)

        // Notify
        val machineConfig = MachineConfig.getMachineByNameEng(gpuTaskInfo.serverNameEng)

        val gpuTaskNotify = GpuTaskNotify(
            gpuTaskInfo = gpuTaskInfo,
            machineConfig = machineConfig
        )

        if (
            !gpuTaskInfo.isDebugMode
            || gpuTaskInfo.multiDeviceLocalRank < 1
        ) {
            gpuTaskNotify.sendTaskMessage()
        }

        val responseObj = ClientResponse()
        responseObj.result = "success"
        responseObj.isSucceed = true
        responseObj.isAuthenticated = true
        return responseObj
    }

}
