package com.khm.group.center.db.model.client

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.khm.group.center.datatype.receive.task.GpuTaskInfo
import java.util.*


@TableName(value = "gpu_task_info")
class GpuTaskInfoModel {

    @TableId(type = IdType.AUTO)
    var id: Long? = null

    // Common part
    var serverName: String = ""
    var serverNameEng: String = ""

    // @TableField(exist = false)
    // var accessKey: String = ""

    var taskId: String = ""

    var messageType: String = ""

    // 任务类型
    var taskType: String = ""

    // 任务状态
    var taskStatus: String = ""

    // 用户
    var taskUser: String = ""

    // 进程PID
    var taskPid: Int = 0
    var taskMainMemory: Int = 0

    // @TableField(exist = false)
    // var allTaskMessage: String = ""

    // GPU 信息
    var gpuUsagePercent: Float = 0f
    var gpuMemoryUsageString: String = ""
    var gpuMemoryFreeString: String = ""
    var gpuMemoryTotalString: String = ""
    var gpuMemoryPercent: Float = 0f

    var taskGpuId: Int = 0
    var taskGpuName: String = ""

    var taskGpuMemoryGb: Float = 0f
    var taskGpuMemoryHuman: String = ""

    var taskGpuMemoryMaxGb: Float = 0f

    var isMultiGpu: Boolean = false
    var multiDeviceLocalRank: Int = 0
    var multiDeviceWorldSize: Int = 0
    var topPythonPid: Int = -1

    var cudaRoot: String = ""
    var cudaVersion: String = ""

    var isDebugMode: Boolean = false

    // 运行时间
    // TimeStamp
    var taskStartTime: Long = 0
    var taskFinishTime: Long = 0

    // Default is Now
    var taskStartTimeObj: Date = Date()
    var taskFinishTimeObj: Date = Date()

    var taskRunningTimeString: String = ""
    var taskRunningTimeInSeconds: Int = 0

    var projectDirectory: String = ""
    var projectName: String = ""
    var screenSessionName: String = ""
    var pyFileName: String = ""

    var pythonVersion: String = ""
    var commandLine: String = ""
    var condaEnvName: String = ""

    fun parse() {
        // Convert Timestamp to Date Object
        try {
            if (taskStartTime > 0) {
                taskStartTimeObj = Date(taskStartTime * 1000)
            }
            if (taskFinishTime > 0) {
                taskFinishTimeObj = Date(taskFinishTime * 1000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copy(gpuTaskInfo: GpuTaskInfo) {
        this.serverName = gpuTaskInfo.serverName
        this.serverNameEng = gpuTaskInfo.serverNameEng

        // this.accessKey = gpuTaskInfo.accessKey

        this.taskId = gpuTaskInfo.taskId

        this.messageType = gpuTaskInfo.messageType

        this.taskType = gpuTaskInfo.taskType

        this.taskStatus = gpuTaskInfo.taskStatus

        this.taskUser = gpuTaskInfo.taskUser

        this.taskPid = gpuTaskInfo.taskPid
        this.taskMainMemory = gpuTaskInfo.taskMainMemory

        // this.allTaskMessage = gpuTaskInfo.allTaskMessage

        this.gpuUsagePercent = gpuTaskInfo.gpuUsagePercent
        this.gpuMemoryUsageString = gpuTaskInfo.gpuMemoryUsageString
        this.gpuMemoryFreeString = gpuTaskInfo.gpuMemoryFreeString
        this.gpuMemoryTotalString = gpuTaskInfo.gpuMemoryTotalString
        this.gpuMemoryPercent = gpuTaskInfo.gpuMemoryPercent

        this.taskGpuId = gpuTaskInfo.taskGpuId
        this.taskGpuName = gpuTaskInfo.taskGpuName

        this.taskGpuMemoryGb = gpuTaskInfo.taskGpuMemoryGb
        this.taskGpuMemoryHuman = gpuTaskInfo.taskGpuMemoryHuman

        this.taskGpuMemoryMaxGb = gpuTaskInfo.taskGpuMemoryMaxGb

        this.isMultiGpu = gpuTaskInfo.isMultiGpu
        this.multiDeviceLocalRank = gpuTaskInfo.multiDeviceLocalRank
        this.multiDeviceWorldSize = gpuTaskInfo.multiDeviceWorldSize
        this.topPythonPid = gpuTaskInfo.topPythonPid

        this.cudaRoot = gpuTaskInfo.cudaRoot
        this.cudaVersion = gpuTaskInfo.cudaVersion

        this.isDebugMode = gpuTaskInfo.isDebugMode

        this.taskStartTime = gpuTaskInfo.taskStartTime
        this.taskFinishTime = gpuTaskInfo.taskFinishTime
        this.taskRunningTimeString = gpuTaskInfo.taskRunningTimeString
        this.taskRunningTimeInSeconds = gpuTaskInfo.taskRunningTimeInSeconds

        this.projectDirectory = gpuTaskInfo.projectDirectory
        this.projectName = gpuTaskInfo.projectName
        this.screenSessionName = gpuTaskInfo.screenSessionName
        this.pyFileName = gpuTaskInfo.pyFileName

        this.pythonVersion = gpuTaskInfo.pythonVersion
        this.commandLine = gpuTaskInfo.commandLine
        this.condaEnvName = gpuTaskInfo.condaEnvName

        this.parse()
    }

    companion object {
        fun fromGpuTaskInfo(gpuTaskInfo: GpuTaskInfo): GpuTaskInfoModel {
            val gpuTaskInfoModel = GpuTaskInfoModel()

            gpuTaskInfoModel.copy(gpuTaskInfo)

            return gpuTaskInfoModel
        }
    }
}
