package com.khm.group.center.datatype.summary

import com.khm.group.center.utils.time.DateTimeUtils

/**
 * 单个GPU任务的详细信息
 * 包含任务ID、启动时间、结束时间等详细信息
 * 用于在用户统计中提供详细的任务信息
 */
data class GpuTaskDetail(
    val taskId: String,
    val gpuName: String,
    val machineName: String,
    val gpuUseTime: Int,
    val startTime: Long,
    val finishTime: Long,
    val projectName: String = "",
    val gpuUsagePercent: Float = 0f,
    val gpuMemoryPercent: Float = 0f,
    val gpuMemoryGb: Float = 0f,
    val taskStatus: String = ""
) {
    
    fun getStartTimeFormatted(): String {
        return if (startTime > 0) {
            DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(startTime))
        } else {
            "N/A"
        }
    }

    fun getFinishTimeFormatted(): String {
        return if (finishTime > 0) {
            DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(finishTime))
        } else {
            "N/A"
        }
    }

    override fun toString(): String {
        return "GpuTaskDetail(" +
               "taskId='$taskId', " +
               "gpuName='$gpuName', " +
               "machineName='$machineName', " +
               "gpuUseTime=$gpuUseTime, " +
               "startTime=${getStartTimeFormatted()}, " +
               "finishTime=${getFinishTimeFormatted()}, " +
               "projectName='$projectName', " +
               "gpuUsage=$gpuUsagePercent%, " +
               "gpuMemory=$gpuMemoryPercent%, " +
               "memoryUsed=${gpuMemoryGb}GB, " +
               "status='$taskStatus')"
    }
}