package com.khm.group.center.datatype.summary

import java.util.*

class TaskSummary(
    val taskId: String,

    val projectName: String,
    val gpuName: String,
    val machineName: String,

    val startTime: Date,

    val runTime: Float = 0f
) {
    val gpuCount: Int = 0

    val pythonVersion: String = ""

    val startTimeString: String
        get() = startTime.toString()

    override fun hashCode(): Int {
        return taskId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return taskId == (other as TaskSummary).taskId
    }
}
