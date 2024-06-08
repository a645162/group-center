package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.receive.GpuTaskInfo
import com.khm.group.center.message.MessageCenter
import com.khm.group.center.message.MessageItem
import com.khm.group.center.datatype.utils.datetime.DateTime
import com.khm.group.center.datatype.utils.common.FloatValue
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
                "[GPU${gpuTaskInfo.taskGpuId}]完成!!\n"
            }

            else -> {
                "[GPU Task]"
            }
        }

        var screenName = if (gpuTaskInfo.screenSessionName.isEmpty()) {
            gpuTaskInfo.condaEnvName
        } else {
            gpuTaskInfo.screenSessionName
        }
        if (screenName.isNotEmpty()) {
            screenName = "[${screenName}]"
        }
        if (screenName.length > 20) {
            screenName += "\n"
        }

        val lineBreakThreshold = 35

        var projectName = gpuTaskInfo.projectName.trim()
        var fileName = gpuTaskInfo.pyFileName.trim()
        if (projectName.isNotEmpty()) {
            projectName = "{${projectName}}"
        }
        if (fileName.isNotEmpty()) {
            fileName = "(${fileName})"
        }
        if (projectName.isNotEmpty() && fileName.isNotEmpty()) {
            if (screenName.endsWith("\n")) {
                if (
                    (
                            projectName.length
                                    + fileName.length
                            ) > lineBreakThreshold
                ) {
                    projectName += "\n"
                } else {
                    projectName += "-"
                }
            } else {
                if (
                    (
                            screenName.length
                                    + projectName.length
                                    + fileName.length
                            ) > lineBreakThreshold
                ) {
                    projectName += "\n"
                } else {
                    projectName += "-"
                }
            }
        }

        val pythonVersion = if (gpuTaskInfo.pythonVersion.isNotEmpty()) {
            "Python:" + gpuTaskInfo.pythonVersion
        } else {
            ""
        }

        val cudaVersion = if (gpuTaskInfo.cudaVersion.isNotEmpty()) {
            " CUDA:" + gpuTaskInfo.cudaVersion.lowercase().replace("v", "")
        } else {
            ""
        }

        val pythonAndCudaVersion = if (
            pythonVersion.isNotEmpty()
            || cudaVersion.isNotEmpty()
        ) {
            (pythonVersion + cudaVersion).trim() + "\n"
        } else {
            ""
        }

        var otherTaskMessage = gpuTaskInfo.allTaskMessage.trim()
        if (otherTaskMessage.isNotEmpty()) {
            otherTaskMessage = "其他任务:\n${otherTaskMessage}"
        } else {
            otherTaskMessage = "暂无其他任务!"
        }

        val multiGpuStr = if (gpuTaskInfo.multiDeviceWorldSize > 1) {
            "\n${gpuTaskInfo.multiDeviceWorldSize}卡任务"
        } else {
            ""
        }

        return (
                firstLine
                        + "${screenName}${projectName}${fileName}\n"

                        + "显存:${FloatValue.round(gpuTaskInfo.taskGpuMemoryGb)}GB "
                        + "运行时长:${gpuTaskInfo.taskRunningTimeString}\n"

                        + "最大显存${FloatValue.round(gpuTaskInfo.taskGpuMemoryMaxGb)}GB\n"

                        + "\n"

                        + "启动时间:${DateTime.getDateTimeStrByPythonTimeStamp(gpuTaskInfo.taskStartTime)}\n"

                        + "\n"

                        + "${gpuTaskInfo.taskGpuName}\n"
                        + "核心(${FloatValue.round(gpuTaskInfo.gpuUsagePercent)}%) "
                        + "空闲显存:${FileSize.fixText(gpuTaskInfo.gpuMemoryFreeString)}\n"

                        + "显存(${FloatValue.round(gpuTaskInfo.gpuMemoryPercent)}%) "
                        + FileSize.fixText(gpuTaskInfo.gpuMemoryUsageString)
                        + "/"
                        + "${FileSize.fixText(gpuTaskInfo.gpuMemoryTotalString)}\n"

                        + pythonAndCudaVersion

                        + multiGpuStr

                        + "\n"

                        + otherTaskMessage
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
