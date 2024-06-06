package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.GpuTaskInfo
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.message.MessageItem
import com.khm.group.center.utils.datetime.DateTime


class GpuTaskNotify(
    var gpuTaskInfo: GpuTaskInfo,
    var machineConfig: MachineConfig?
) {
    fun generateTaskStartMessage(gpuTaskInfo: GpuTaskInfo): String {
        val maxMemoryString = String.format("%.2d", gpuTaskInfo.taskGpuMemoryMaxGb)
        return (
                "[GPU${gpuTaskInfo.taskGpuId}]启动->\n"
                        + "[${gpuTaskInfo.condaEnvName}]${gpuTaskInfo.projectName}\n"
                        + "最大显存:${maxMemoryString}GB "
                        + "运行时长:${gpuTaskInfo.taskRunningTimeString} "
                        + "\n"
                        + "启动时间:${DateTime.getDateTimeStrByPythonTimeStamp(gpuTaskInfo.taskStartTime)}"
                )
    }

    fun generateTaskFinishMessage(gpuTaskInfo: GpuTaskInfo): String {
        val maxMemoryString = String.format("%.2d", gpuTaskInfo.taskGpuMemoryMaxGb)
        return (
                "[GPU${gpuTaskInfo.taskGpuId}]完成!\n"
                        + "[${gpuTaskInfo.condaEnvName}]${gpuTaskInfo.projectName}\n"
                        + "最大显存:${maxMemoryString}GB "
                        + "运行时长:${gpuTaskInfo.taskRunningTimeString} "
                        + "\n"
                        + "启动时间:${DateTime.getDateTimeStrByPythonTimeStamp(gpuTaskInfo.taskStartTime)}"
                )
    }

    fun sendTaskMessage() {
        if (machineConfig == null) {
            return
        }

        // Send Message
        var finalText = ""

        // Generate Text
        when (gpuTaskInfo.messageType) {
            "create" -> {
                finalText = generateTaskStartMessage(gpuTaskInfo)
            }

            "finish" -> {
                finalText = generateTaskFinishMessage(gpuTaskInfo)
            }
        }

        finalText = finalText.trim()

        val messageItem = MessageItem(
            content = finalText,
            targetUser = gpuTaskInfo.taskUser,
            machineConfig = machineConfig!!
        )
        MessageCenter.addNewMessage(messageItem)
    }

}
