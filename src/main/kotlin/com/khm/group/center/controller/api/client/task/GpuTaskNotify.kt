package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.task.GpuTaskInfo
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.message.MessageItem
import com.khm.group.center.datatype.utils.datetime.DateTime
import com.khm.group.center.datatype.utils.common.FloatValue
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.file.FileSize


class GpuTaskNotify(
    var gpuTaskInfo: GpuTaskInfo,
    var machineConfig: MachineConfig?,
    var multiGpuTaskInfoModel: List<GpuTaskInfoModel>?
) {
    private fun getGpuId(): String {
        val defaultStr = "${gpuTaskInfo.taskGpuId}"
        if (
            gpuTaskInfo.multiDeviceWorldSize < 2 ||
            multiGpuTaskInfoModel == null ||
            multiGpuTaskInfoModel!!.isEmpty()
        ) {
            return defaultStr
        }

        var gpuIdList = mutableListOf<Int>()

        for (gpuTaskInfoModel in multiGpuTaskInfoModel!!) {
            gpuIdList.add(gpuTaskInfoModel.taskGpuId)
        }

        gpuIdList = gpuIdList.distinct().toMutableList()

        val isRecordCorrect = (gpuIdList.size == gpuTaskInfo.multiDeviceWorldSize)

        // Sort
        gpuIdList.sort()

        // 检查卡Index是否连续
        var isContinuous = true
        for (i in 0 until gpuIdList.size - 1) {
            if (gpuIdList[i] + 1 != gpuIdList[i + 1]) {
                isContinuous = false
                break
            }
        }

        // 记录正确的情况下，且不连续
        if (isRecordCorrect && !isContinuous) {
            sendGpuWarningMessage(
                "GPU使用非连续警告",
                "检测到GPU使用不连续：${gpuIdList.joinToString(",")}。\n\n建议使用连续的GPU以获得更好的性能。"
            )
        }

        // 跨NUMA节点检测
        val totalGpuCount = gpuTaskInfo.totalGpuCount
        if (totalGpuCount >= 4) {
            // 4卡及以上机器，前一半卡在NUMA1，后一半卡在NUMA2
            val numa1MaxId = totalGpuCount / 2 - 1  // NUMA1的最大GPU ID

            val isUseAllGpu = (gpuIdList.size == totalGpuCount)

            // 检查使用的GPU是否跨越NUMA边界
            val hasNuma1Gpu = gpuIdList.any { it <= numa1MaxId }
            val hasNuma2Gpu = gpuIdList.any { it > numa1MaxId }

            if (!isUseAllGpu && hasNuma1Gpu && hasNuma2Gpu) {
                sendGpuWarningMessage(
                    "跨NUMA节点警告",
                    "使用GPU卡${gpuIdList.joinToString(",")}" +
                            "跨越NUMA节点边界(边界在GPU${numa1MaxId}/${numa1MaxId + 1}之间)。" +
                            "\n\n这可能影响性能，建议在同一NUMA节点内使用GPU。"
                )
            }
        }

        if (!isRecordCorrect) {
            return defaultStr
        }

        return gpuIdList.joinToString(",")
    }

    private fun generateTaskMessage(): String {
        var timeString = ""

        val gpuIdString = getGpuId()

        val firstLine = when (gpuTaskInfo.messageType) {
            "create" -> {
                timeString =
                    DateTime.getDateTimeStrByPythonTimeStamp(
                        gpuTaskInfo.taskStartTime
                    )

                "[GPU${gpuIdString}]启动->\n"
            }

            "finish" -> {
                if (gpuTaskInfo.taskFinishTime > 0) {
                    timeString =
                        DateTime.getDateTimeStrByPythonTimeStamp(
                            gpuTaskInfo.taskFinishTime
                        )
                }

                "[GPU${gpuIdString}]完成!!!\n"
            }

            else -> {
                "[GPU Task]"
            }
        }

        var screenName =
            gpuTaskInfo.screenSessionName.ifEmpty {
                gpuTaskInfo.condaEnvName
            }
        if (screenName.isNotEmpty()) {
            screenName = "[${screenName}]"
        }
        var projectName = gpuTaskInfo.projectName.trim()
        var fileName = gpuTaskInfo.pyFileName.trim()
        if (projectName.isNotEmpty()) {
            projectName = "{${projectName}}"
        }
        if (fileName.isNotEmpty()) {
            fileName = "(${fileName})"
        }

        var otherTaskMessage = gpuTaskInfo.allTaskMessage.trim()
        val otherTaskCount = otherTaskMessage.split("\n").size
        otherTaskMessage =
            if (otherTaskMessage.isNotEmpty()) {
                "GPU${gpuTaskInfo.taskGpuId}任务(${otherTaskCount}个):\n${otherTaskMessage}"
            } else {
                "[GPU${gpuTaskInfo.taskGpuId}]暂无任务!"
            }

        val multiGpuStr = if (gpuTaskInfo.multiDeviceWorldSize > 1) {
            var extraInfo = ""

            if (gpuIdString != "${gpuTaskInfo.taskGpuId}") {
                extraInfo += "(${gpuIdString})"
            }

            "\n${gpuTaskInfo.multiDeviceWorldSize}卡任务${extraInfo}\n"
        } else {
            ""
        }

        return (
                firstLine
                        + "${screenName}${projectName}${fileName}"
                        + "用时:${gpuTaskInfo.taskRunningTimeString}，"
                        + "最大显存${FloatValue.round(gpuTaskInfo.taskGpuMemoryMaxGb)}GB\n"

                        + "\n"

                        + "核心占用(${FloatValue.round(gpuTaskInfo.gpuUsagePercent)}%)，"
                        + "显存占用"
                        + FileSize.fixText(gpuTaskInfo.gpuMemoryUsageString)
                        + "/"
                        + "${FileSize.fixText(gpuTaskInfo.gpuMemoryTotalString)} " +
                        "(${FloatValue.round(gpuTaskInfo.gpuMemoryPercent)}%)，"
                        + "${FileSize.fixText(gpuTaskInfo.gpuMemoryFreeString)}空闲\n"

                        + multiGpuStr

                        + "\n"

                        + otherTaskMessage + "\n"

                        + "\n"

                        + timeString
                )
    }

    private fun sendGpuWarningMessage(warningType: String, warningContent: String) {
        if (machineConfig == null) {
            return
        }

        val timeString = DateTime.getDateTimeStrByPythonTimeStamp(System.currentTimeMillis() / 1000)

        val warningMessage = buildString {
            append("⚠️ ${warningType} ⚠️\n\n")
            append("机器：${machineConfig!!.name}\n")
            append("用户：${gpuTaskInfo.taskUser}\n")
            append("任务：${gpuTaskInfo.projectName.ifEmpty { gpuTaskInfo.pyFileName }}\n")
            append("GPU数量：${gpuTaskInfo.multiDeviceWorldSize}卡\n\n")
            append("${warningContent}\n\n")
            append("时间：${timeString}")
        }

        val messageItem = MessageItem(
            content = warningMessage,
            targetUser = gpuTaskInfo.taskUser,
            machineConfig = machineConfig!!,
            sendToPersonBot = true,
            sendToGroupBot = true,
            groupAt = gpuTaskInfo.taskUser, // 在群里@用户
        )
        MessageCenter.addNewMessage(messageItem)
    }

    fun sendTaskMessage() {
        if (machineConfig == null) {
            return
        }

        // Send Message
        var finalText = generateTaskMessage()

        finalText = finalText.trim()

        val messageItem = MessageItem(
            content = finalText,
            targetUser = gpuTaskInfo.taskUser,
            machineConfig = machineConfig!!,
            sendToPersonBot = true,
            sendToGroupBot = true,
            groupAt = "",
        )
        MessageCenter.addNewMessage(messageItem)
    }

}
