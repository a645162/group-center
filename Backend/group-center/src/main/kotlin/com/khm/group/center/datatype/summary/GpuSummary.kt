package com.khm.group.center.datatype.summary

class GpuSummary(
    val gpuName: String,
    val machineName: String,
    var gpuUseTime: Int,
) {
    fun addUseTime(time: Int) {
        gpuUseTime += time
    }

    fun newTask(taskSummary: TaskSummary) {
        addUseTime(taskSummary.runTime.toInt())
    }
}
