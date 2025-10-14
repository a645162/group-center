package com.khm.group.center.controller.api.client.task

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.service.GpuTaskNotifyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

import com.khm.group.center.datatype.receive.task.GpuTaskInfo
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.mapper.client.GpuTaskInfoMapper
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired


@RestController
@Tag(name = "GPU Task", description = "GPU task information submission and notification API")
class GpuTaskController {

    @Autowired
    lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    @Autowired
    lateinit var gpuTaskNotifyService: GpuTaskNotifyService

    @Operation(summary = "GPU任务变动")
    @RequestMapping("/api/client/gpu_task/info", method = [RequestMethod.POST])
    fun postGpuTaskInfo(@RequestBody gpuTaskInfo: GpuTaskInfo): ClientResponse {
        // Update DB
        updateDbByObject(gpuTaskInfo)

        // Create Response Object
        val responseObj = ClientResponse()
        responseObj.result = "success"
        responseObj.isSucceed = true
        responseObj.isAuthenticated = true

        println(
            "Receive task from nvi-notify" +
                    " [${gpuTaskInfo.taskType}]" +
                    " Project:${gpuTaskInfo.projectName}" +
                    " User:${gpuTaskInfo.taskUser}"
        )

        // 新增判断，如果为update类型，只更新数据库，直接返回
        if (gpuTaskInfo.messageType == "update") {
            return responseObj
        }

        // Notify in a separate coroutine
        CoroutineScope(Dispatchers.IO).launch {
            newTaskNotify(gpuTaskInfo)
        }

        // 如果是任务完成消息，触发订阅通知
        if (gpuTaskInfo.messageType == "finish") {
            CoroutineScope(Dispatchers.IO).launch {
                handleTaskCompletionNotification(gpuTaskInfo)
            }
        }

        return responseObj
    }

    private fun updateDbByObject(gpuTaskInfo: GpuTaskInfo) {
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

    private fun newTaskNotify(gpuTaskInfo: GpuTaskInfo) {


        val machineConfig = MachineConfig.getMachineByNameEng(gpuTaskInfo.serverNameEng)

        val isMultiCard = gpuTaskInfo.multiDeviceWorldSize > 1

        var multiGpuTaskInfoModel: List<GpuTaskInfoModel>? = null

        if (isMultiCard && gpuTaskInfo.multiDeviceLocalRank == 0) {
            var waitTimes = 0
            val waitTimeThreshold = 10

            // Wait for all the task to be ready
            while (
                (multiGpuTaskInfoModel == null
                        || multiGpuTaskInfoModel.size != gpuTaskInfo.multiDeviceWorldSize)
                && waitTimes <= waitTimeThreshold
            ) {
                multiGpuTaskInfoModel = getMultiGpuTaskInfoModel(gpuTaskInfo)
                if (multiGpuTaskInfoModel.size == gpuTaskInfo.multiDeviceWorldSize) {
                    break
                }

                waitTimes++
                Thread.sleep(1000)
            }
        }

        val gpuTaskNotify = GpuTaskNotify(
            gpuTaskInfo = gpuTaskInfo,
            machineConfig = machineConfig,
            multiGpuTaskInfoModel = multiGpuTaskInfoModel
        )

        if (
            !gpuTaskInfo.isDebugMode &&
            (!isMultiCard || gpuTaskInfo.multiDeviceLocalRank == 0)
        ) {
            gpuTaskNotify.sendTaskMessage()
        }
    }

    private fun getGpuTaskInfoByTaskId(taskId: String): GpuTaskInfoModel? {
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()
        queryWrapper.eq("task_id", taskId)
        return gpuTaskInfoMapper.selectOne(queryWrapper)
    }

    private fun getMultiGpuTaskInfoModel(gpuTaskInfo: GpuTaskInfo): List<GpuTaskInfoModel> {
        val queryWrapper = QueryWrapper<GpuTaskInfoModel>()

        if (gpuTaskInfo.topPythonPid <= 1) {
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

    /**
     * 处理任务完成时的订阅通知
     */
    private suspend fun handleTaskCompletionNotification(gpuTaskInfo: GpuTaskInfo) {
        try {
            val projectId = gpuTaskInfo.projectName
            val taskId = gpuTaskInfo.taskId
            val taskName = gpuTaskInfo.pyFileName.ifEmpty { gpuTaskInfo.projectName }

            // 异步触发订阅通知
            gpuTaskNotifyService.notifyTaskCompletionAsync(projectId, taskId, taskName)
        } catch (e: Exception) {
            println("处理任务完成通知失败: ${e.message}")
        }
    }

}
