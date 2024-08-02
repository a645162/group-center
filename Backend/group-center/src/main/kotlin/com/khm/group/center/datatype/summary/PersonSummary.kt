package com.khm.group.center.datatype.summary

class PersonSummary(
    val personName: String,
    val personNameEng: String,
    var personUseTime: Int
) {
    data class TaskSummary(
        val taskId: String,
        val projectName: String,
        val startTime: String,
        val runTime: Float,
        val gpuCount: Int,
        var taskUseTimes: Int
    )
}
