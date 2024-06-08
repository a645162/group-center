package com.khm.group.center.datatype.receive

class GpuTaskInfo {
    // Common part
    var serverName: String = ""
    var serverNameEng: String = ""
    var accessKey: String = ""

    var taskID: String = ""

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

    //    var taskGpuMemoryMb: Long = 0;
    var taskGpuMemoryGb: Float = 0f
    var taskGpuMemoryHuman: String = ""

    //    var taskGpuMemoryMaxMb: Long = 0;
    var taskGpuMemoryMaxGb: Float = 0f

    var multiDeviceLocalRank: Int = 0
    var multiDeviceWorldSize: Int = 0

    var cudaRoot: String = ""
    var cudaVersion: String = ""

    // 运行时间
    var taskStartTime: Long = 0
    var taskRunningTimeString: String = ""
    var taskRunningTimeInSeconds: Int = 0

    var projectName: String = ""
    var screenSessionName: String = ""
    var pyFileName: String = ""

    var pythonVersion: String = ""
    var commandLine: String = ""
    var condaEnvName: String = ""
}
