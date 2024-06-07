package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.GpuTaskInfo
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.message.MessageItem
import com.khm.group.center.utils.datetime.DateTime
import com.khm.group.center.utils.datetype.FloatValue
import com.khm.group.center.utils.file.FileSize


class GpuTaskNotify(
    var gpuTaskInfo: GpuTaskInfo,
    var machineConfig: MachineConfig?
) {
    private fun generateTaskMessage(): String {
        val firstLine = when (gpuTaskInfo.messageType) {
            "create" -> {
                "[GPU${gpuTaskInfo.taskGpuId}]启动->\n"
            }

            "finish" -> {
                "[GPU${gpuTaskInfo.taskGpuId}]完成!\n"
            }

            else -> {
                ""
            }
        }

        var screen_name = gpuTaskInfo.screenSessionName;
        if (screen_name.isEmpty()) {
            screen_name = gpuTaskInfo.condaEnvName;
        }

        return (
                firstLine
                        + "[${screen_name}]${gpuTaskInfo.projectName}-${gpuTaskInfo.pyFileName}\n"

                        + "显存:${FloatValue.round(gpuTaskInfo.taskGpuMemoryGb)}GB "
                        + "运行时长:${gpuTaskInfo.taskRunningTimeString} "
                        + "最大显存${FloatValue.round(gpuTaskInfo.taskGpuMemoryMaxGb)}GB\n"

                        + "\n"

                        + "启动时间:${DateTime.getDateTimeStrByPythonTimeStamp(gpuTaskInfo.taskStartTime)}\n"

                        + "\n"

                        + "核心(${FloatValue.round(gpuTaskInfo.gpuUsagePercent)}%) "
                        + "空闲显存:${FileSize.fixText(gpuTaskInfo.gpuMemoryFreeString)}\n"

                        + "显存(${FloatValue.round(gpuTaskInfo.gpuMemoryPercent)}%) "
                        + "${FileSize.fixText(gpuTaskInfo.gpuMemoryUsageString)}"
                        + "/"
                        + "${FileSize.fixText(gpuTaskInfo.gpuMemoryTotalString)}\n"

                        + "\n"

                        + gpuTaskInfo.allTaskMessage
                )
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
            machineConfig = machineConfig!!
        )
        MessageCenter.addNewMessage(messageItem)
    }

}
