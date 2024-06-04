package com.khm.group.center.controller.api.client.task

import com.khm.group.center.datatype.receive.GpuTaskInfo


class GpuTaskNotify {

    companion object {

        fun notifyGpuTaskInfo(gpuTaskInfo: GpuTaskInfo) {

        }

        fun generateTaskStartMessage(gpuTaskInfo: GpuTaskInfo): String {
            return ""
        }

        fun generateTaskFinishMessage(gpuTaskInfo: GpuTaskInfo): String {
            return ""
        }
    }

}