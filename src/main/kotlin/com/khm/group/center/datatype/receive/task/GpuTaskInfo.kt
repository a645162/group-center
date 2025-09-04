package com.khm.group.center.datatype.receive.task

class GpuTaskInfo {

    // Common part
    var serverName: String = ""
    var serverNameEng: String = ""

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

    var allTaskMessage: String = ""

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
    var taskStartTime: Long = 0
    var taskFinishTime: Long = 0
    var taskRunningTimeString: String = ""
    var taskRunningTimeInSeconds: Int = 0

    var projectDirectory: String = ""
    var projectName: String = ""
    var screenSessionName: String = ""
    var pyFileName: String = ""

    var pythonVersion: String = ""
    var commandLine: String = ""
    var condaEnvName: String = ""

    var totalGpuCount: Int = 0
}