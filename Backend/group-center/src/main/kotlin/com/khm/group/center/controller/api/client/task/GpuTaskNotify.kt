package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.GpuTaskInfo
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.message.MessageItem


class GpuTaskNotify(
    var gpuTaskInfo: GpuTaskInfo,
    var machineConfig: MachineConfig?
) {

    fun generateTaskStartMessage(gpuTaskInfo: GpuTaskInfo): String {
        return (
                "[GPU${gpuTaskInfo.taskGpuId}]启动->\n"
                        + "用户:${gpuTaskInfo.taskUser} "
                        + "最大显存:${gpuTaskInfo.taskGpuMemoryMaxMb} "
                        + "运行时长:${gpuTaskInfo.taskRunningTimeString} "
                )
    }

    fun generateTaskFinishMessage(gpuTaskInfo: GpuTaskInfo): String {
        return (
                "[GPU${gpuTaskInfo.taskGpuId}]完成!\n"
                        + "用户:${gpuTaskInfo.taskUser} "
                        + "最大显存:${gpuTaskInfo.taskGpuMemoryMaxMb} "
                        + "运行时长:${gpuTaskInfo.taskRunningTimeString} "
                )
    }

    fun sendTaskMessage() {
        if (machineConfig == null) {
            return
        }

        // Send Message
        var finalText = ""

        // Generate Text
        when (gpuTaskInfo.taskStatus) {
            "create" -> {
                finalText = generateTaskStartMessage(gpuTaskInfo)
            }

            "finish" -> {
                finalText = generateTaskFinishMessage(gpuTaskInfo)
            }
        }

        finalText = finalText.trim()

        val messageItem= MessageItem(
            content = finalText,
            targetUser = gpuTaskInfo.taskUser,
            machineConfig = machineConfig!!
        )
        MessageCenter.addNewMessage(messageItem)
    }

}
