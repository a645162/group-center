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
        var finishTimeString = ""

        val firstLine = when (gpuTaskInfo.messageType) {
            "create" -> {
                "[GPU${gpuTaskInfo.taskGpuId}]启动->\n"
            }

            "finish" -> {
                if (gpuTaskInfo.taskFinishTime > 0) {
                    finishTimeString = (
                            "结束时间:" +
                                    DateTime.getDateTimeStrByPythonTimeStamp(
                                        gpuTaskInfo.taskFinishTime
                                    )
                                    + "\n"
                            )
                }

                "[GPU${gpuTaskInfo.taskGpuId}]完成!!\n"
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
                projectName += if (
                    (
                            projectName.length
                                    + fileName.length
                            ) > lineBreakThreshold
                ) {
                    "\n"
                } else {
                    "-"
                }
            } else {
                projectName += if (
                    (
                            screenName.length
                                    + projectName.length
                                    + fileName.length
                            ) > lineBreakThreshold
                ) {
                    "\n"
                } else {
                    "-"
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
        val otherTaskCount = otherTaskMessage.split("\n").size
        otherTaskMessage =
            if (otherTaskMessage.isNotEmpty()) {
                "当前GPU任务(${otherTaskCount}个):\n${otherTaskMessage}"
            } else {
                "暂无任务!"
            }

        val multiGpuStr = if (gpuTaskInfo.multiDeviceWorldSize > 1) {
            "\n${gpuTaskInfo.multiDeviceWorldSize}卡任务\n"
        } else {
            ""
        }

        return (
                firstLine
                        + "${screenName}${projectName}${fileName}\n"

                        + "\n"

                        + "运行时长:${gpuTaskInfo.taskRunningTimeString}\n"
                        + "启动时间:${DateTime.getDateTimeStrByPythonTimeStamp(gpuTaskInfo.taskStartTime)}\n"
                        + finishTimeString

                        + "\n"

                        + "当前显存:${FloatValue.round(gpuTaskInfo.taskGpuMemoryGb)}GB "
                        + "最大显存${FloatValue.round(gpuTaskInfo.taskGpuMemoryMaxGb)}GB\n"
                        + pythonAndCudaVersion

                        + "\n"

                        + "${gpuTaskInfo.taskGpuName}\n"

                        + "\n"

                        + "核心(${FloatValue.round(gpuTaskInfo.gpuUsagePercent)}%) "
                        + "空闲显存:${FileSize.fixText(gpuTaskInfo.gpuMemoryFreeString)}\n"

                        + "显存(${FloatValue.round(gpuTaskInfo.gpuMemoryPercent)}%) "
                        + FileSize.fixText(gpuTaskInfo.gpuMemoryUsageString)
                        + "/"
                        + "${FileSize.fixText(gpuTaskInfo.gpuMemoryTotalString)}\n"

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
