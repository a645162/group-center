package com.khm.group.center.controller.api.client.task

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.datatype.config.MachineConfig
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

import com.khm.group.center.datatype.receive.task.GpuTaskInfo
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

    private fun getMultiGpuTaskInfoModel(gpuTaskInfo: GpuTaskInfo): List<GpuTaskInfoModel> {
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()

        if (gpuTaskInfo.topPythonPid == -1) {
            return listOf()
        }

        queryWrapper
            // Parent PID
            .eq("top_python_pid", gpuTaskInfo.topPythonPid)
            // World Size should be the same
            .eq("multi_device_world_size", gpuTaskInfo.multiDeviceWorldSize)
            // User should be the same
            .eq("task_user", gpuTaskInfo.taskUser)
            // Machine should be the same
            .eq("server_name_eng", gpuTaskInfo.serverNameEng)
            // Project should be the same
            .eq("project_name", gpuTaskInfo.projectName)
            .eq("project_directory", gpuTaskInfo.projectDirectory)

        return gpuTaskInfoMapper.selectList(queryWrapper)
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

        println(
            "Receive task from nvi-notify" +
                    " ${gpuTaskInfo.taskType}" +
                    " Project:${gpuTaskInfo.projectName}" +
                    " User:${gpuTaskInfo.taskUser}"
        )

        val multiGpuTaskInfoModel =
            if (gpuTaskInfo.multiDeviceWorldSize > 1) {
                getMultiGpuTaskInfoModel(gpuTaskInfo)
            } else {
                null
            }

        val gpuTaskNotify = GpuTaskNotify(
            gpuTaskInfo = gpuTaskInfo,
            machineConfig = machineConfig,
            multiGpuTaskInfoModel = multiGpuTaskInfoModel
        )

        val isMultiCard = gpuTaskInfo.multiDeviceWorldSize > 1

        if (
            !gpuTaskInfo.isDebugMode &&
            (!isMultiCard || gpuTaskInfo.multiDeviceLocalRank == 0)
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
