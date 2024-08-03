package com.khm.group.center.datatype.summary

import com.khm.group.center.utils.time.GenshinImpactTime

class PersonSummary(
    val personName: String,
    val personNameEng: String,
    var personUseTime: Int
) {
    // 卡型号的使用时间
    val gpuModelUseTime = mutableListOf<GpuSummary>()

    val projectList = mutableListOf<ProjectSummary>()

    // (每日)最晚启动的任务
    var latestStartTask: TaskSummary? = null

    // 用卡数量最多的任务
    var mostGpuTask: TaskSummary? = null

    // 用卡时间最长的任务
    var longestGpuTask: TaskSummary? = null

    fun newTask(taskSummary: TaskSummary) {
        for (project in projectList) {
            if (project.updateByTask(taskSummary)) {

                break
            }
        }

        // 更新最晚启动的任务
        if (latestStartTask == null) {
            latestStartTask = taskSummary
        } else {
            val oldTimeObj = GenshinImpactTime.from(latestStartTask!!.startTime)
            val newTimeObj = GenshinImpactTime.from(taskSummary.startTime)

            if (
                latestStartTask!!.startTime.before(taskSummary.startTime)
                && oldTimeObj == newTimeObj
            ) {
                latestStartTask = taskSummary
            }
        }

        // 更新用卡数量最多的任务
        if (mostGpuTask == null) {
            mostGpuTask = taskSummary
        } else {
            if (mostGpuTask!!.gpuCount < taskSummary.gpuCount) {
                mostGpuTask = taskSummary
            }
        }

        // 更新用卡时间最长的任务
        if (longestGpuTask == null) {
            longestGpuTask = taskSummary
        } else {
            if (longestGpuTask!!.runTime < taskSummary.runTime) {
                longestGpuTask = taskSummary
            }
        }
    }
}
