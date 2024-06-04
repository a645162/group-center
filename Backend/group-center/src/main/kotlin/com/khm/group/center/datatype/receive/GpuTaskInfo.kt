package com.khm.group.center.datatype.receive

class GpuTaskInfo {
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

    var taskGpuMemoryMb: Int = 0;
    var taskGpuMemoryHuman: String = "";

    var taskGpuMemoryMaxMb: Int = 0;

    // 运行时间
    var startTime: Float = 0f;

    var taskRunningTime: String = "";
    var taskRunningTimeInSeconds: Int = 0;
}
