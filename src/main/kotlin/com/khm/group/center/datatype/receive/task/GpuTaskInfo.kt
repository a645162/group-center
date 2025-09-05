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

    // GPU 占用信息
    var gpuUsagePercent: Float = 0f
    var gpuMemoryUsageString: String = ""
    var gpuMemoryFreeString: String = ""
    var gpuMemoryTotalString: String = ""
    var gpuMemoryPercent: Float = 0f

    // GPU Index
    var taskGpuId: Int = 0
    // GPU 名称
    var taskGpuName: String = ""

    // 任务显存占用
    var taskGpuMemoryGb: Float = 0f
    var taskGpuMemoryHuman: String = ""

    // 最大显存占用
    var taskGpuMemoryMaxGb: Float = 0f

    // 多卡
    var isMultiGpu: Boolean = false
    var multiDeviceLocalRank: Int = 0
    var multiDeviceWorldSize: Int = 0
    // 根进程PID
    var topPythonPid: Int = -1

    // CUDA 信息
    var cudaRoot: String = ""
    var cudaVersion: String = ""

    // 是否为调试模式
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