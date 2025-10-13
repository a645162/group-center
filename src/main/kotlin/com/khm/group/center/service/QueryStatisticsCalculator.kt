package com.khm.group.center.service

import com.khm.group.center.datatype.query.QueryStatistics
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger

/**
 * 查询统计计算器
 */
@Slf4jKt
class QueryStatisticsCalculator {

    /**
     * 计算查询结果的统计信息
     */
    fun calculateStatistics(tasks: List<GpuTaskInfoModel>): QueryStatistics {
        if (tasks.isEmpty()) {
            logger.debug("Task list is empty, return empty statistics")
            return QueryStatistics.empty()
        }

        logger.debug("Start calculating statistics for ${tasks.size} tasks")

        return QueryStatistics(
            totalTasks = tasks.size,
            totalRunningTime = calculateTotalRunningTime(tasks),
            avgGpuUsage = calculateAvgGpuUsage(tasks),
            maxGpuUsage = calculateMaxGpuUsage(tasks),
            avgGpuMemoryUsage = calculateAvgGpuMemoryUsage(tasks),
            maxGpuMemoryUsage = calculateMaxGpuMemoryUsage(tasks),
            totalGpuMemoryUsed = calculateTotalGpuMemoryUsed(tasks),
            multiGpuTaskCount = countMultiGpuTasks(tasks),
            debugModeTaskCount = countDebugModeTasks(tasks),
            avgRunningTime = calculateAvgRunningTime(tasks),
            userDistribution = calculateUserDistribution(tasks),
            projectDistribution = calculateProjectDistribution(tasks),
            deviceDistribution = calculateDeviceDistribution(tasks),
            taskTypeDistribution = calculateTaskTypeDistribution(tasks),
            earliestStartTime = findEarliestStartTime(tasks),
            latestFinishTime = findLatestFinishTime(tasks)
        )
    }

    /**
     * 计算总运行时长
     */
    private fun calculateTotalRunningTime(tasks: List<GpuTaskInfoModel>): Long {
        return tasks.sumOf { it.taskRunningTimeInSeconds.toLong() }
    }

    /**
     * 计算平均GPU使用率
     */
    private fun calculateAvgGpuUsage(tasks: List<GpuTaskInfoModel>): Double {
        val validTasks = tasks.filter { it.gpuUsagePercent > 0 }
        return if (validTasks.isNotEmpty()) {
            validTasks.map { it.gpuUsagePercent.toDouble() }.average()
        } else {
            0.0
        }
    }

    /**
     * 计算最大GPU使用率
     */
    private fun calculateMaxGpuUsage(tasks: List<GpuTaskInfoModel>): Double {
        return tasks.maxOfOrNull { it.gpuUsagePercent.toDouble() } ?: 0.0
    }

    /**
     * 计算平均显存使用率
     */
    private fun calculateAvgGpuMemoryUsage(tasks: List<GpuTaskInfoModel>): Double {
        val validTasks = tasks.filter { it.gpuMemoryPercent > 0 }
        return if (validTasks.isNotEmpty()) {
            validTasks.map { it.gpuMemoryPercent.toDouble() }.average()
        } else {
            0.0
        }
    }

    /**
     * 计算最大显存使用率
     */
    private fun calculateMaxGpuMemoryUsage(tasks: List<GpuTaskInfoModel>): Double {
        return tasks.maxOfOrNull { it.gpuMemoryPercent.toDouble() } ?: 0.0
    }

    /**
     * 计算总显存使用量
     */
    private fun calculateTotalGpuMemoryUsed(tasks: List<GpuTaskInfoModel>): Double {
        return tasks.sumOf { it.taskGpuMemoryGb.toDouble() }
    }

    /**
     * 计算多卡任务数量
     */
    private fun countMultiGpuTasks(tasks: List<GpuTaskInfoModel>): Int {
        return tasks.count { it.isMultiGpu }
    }

    /**
     * 计算调试模式任务数量
     */
    private fun countDebugModeTasks(tasks: List<GpuTaskInfoModel>): Int {
        return tasks.count { it.isDebugMode }
    }

    /**
     * 计算平均运行时长
     */
    private fun calculateAvgRunningTime(tasks: List<GpuTaskInfoModel>): Double {
        return if (tasks.isNotEmpty()) {
            tasks.map { it.taskRunningTimeInSeconds.toDouble() }.average()
        } else {
            0.0
        }
    }

    /**
     * 计算用户分布
     */
    private fun calculateUserDistribution(tasks: List<GpuTaskInfoModel>): Map<String, Int> {
        return tasks.groupingBy { it.taskUser }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    /**
     * 计算项目分布
     */
    private fun calculateProjectDistribution(tasks: List<GpuTaskInfoModel>): Map<String, Int> {
        return tasks.groupingBy { it.projectName }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    /**
     * 计算设备分布
     */
    private fun calculateDeviceDistribution(tasks: List<GpuTaskInfoModel>): Map<String, Int> {
        return tasks.groupingBy { it.serverNameEng }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    /**
     * 计算任务类型分布
     */
    private fun calculateTaskTypeDistribution(tasks: List<GpuTaskInfoModel>): Map<String, Int> {
        return tasks.groupingBy { it.taskType }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    /**
     * 查找最早开始时间
     */
    private fun findEarliestStartTime(tasks: List<GpuTaskInfoModel>): Long? {
        return tasks.minOfOrNull { it.taskStartTime }
    }

    /**
     * 查找最晚结束时间
     */
    private fun findLatestFinishTime(tasks: List<GpuTaskInfoModel>): Long? {
        return tasks.maxOfOrNull { it.taskFinishTime }
    }
}