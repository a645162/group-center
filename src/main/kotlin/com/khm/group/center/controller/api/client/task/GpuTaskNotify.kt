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

        if (gpuIdList.size != gpuTaskInfo.multiDeviceWorldSize) {
            return defaultStr
        }

        // Sort
        gpuIdList.sort()

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
