package com.khm.group.center.datatype.receive

class GpuTaskInfo {
    // Common part
    var serverName: String = "";
    var serverNameEng: String = "";
    var accessKey: String = "";

    var taskID: Int = 0;

    var messageType: String = "";

    // 任务类型
    var taskType: String = "";

    // 任务状态
    var taskStatus: String = "";

    // 用户
    var taskUser: String = "";

    // 进程PID
    var taskPid: Int = 0;
    var taskMainMemory: Int = 0;

    // GPU 信息
    var taskGpuId: Int = 0;
    var taskGpuName: String = "";

//    var taskGpuMemoryMb: Long = 0;
    var taskGpuMemoryGb: Float = 0f;
    var taskGpuMemoryHuman: String = "";

//    var taskGpuMemoryMaxMb: Long = 0;
    var taskGpuMemoryMaxGb: Float = 0f;

    // 运行时间
    var taskStartTime: Float = 0f;

    var taskRunningTimeString: String = "";
    var taskRunningTimeInSeconds: Int = 0;
}
