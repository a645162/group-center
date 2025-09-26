package com.khm.group.center.datatype.query

/**
 * 查询统计信息
 */
data class QueryStatistics(
    // 基础统计
    val totalTasks: Int,
    val totalRunningTime: Long, // 总运行时长（秒）
    
    // GPU资源统计
    val avgGpuUsage: Double,           // 平均GPU使用率
    val maxGpuUsage: Double,           // 最大GPU使用率
    val avgGpuMemoryUsage: Double,     // 平均显存使用率
    val maxGpuMemoryUsage: Double,     // 最大显存使用率
    val totalGpuMemoryUsed: Double,    // 总显存使用量（GB）
    
    // 任务特征统计
    val multiGpuTaskCount: Int,        // 多卡任务数量
    val debugModeTaskCount: Int,       // 调试模式任务数量
    val avgRunningTime: Double,        // 平均运行时长（秒）
    
    // 分布统计
    val userDistribution: Map<String, Int>,     // 用户分布
    val projectDistribution: Map<String, Int>,  // 项目分布
    val deviceDistribution: Map<String, Int>,   // 设备分布
    val taskTypeDistribution: Map<String, Int>, // 任务类型分布
    
    // 时间统计
    val earliestStartTime: Long?,      // 最早开始时间
    val latestFinishTime: Long?        // 最晚结束时间
) {
    /**
     * 获取平均运行时长（格式化）
     */
    fun getFormattedAvgRunningTime(): String {
        return formatSeconds(avgRunningTime.toLong())
    }
    
    /**
     * 获取总运行时长（格式化）
     */
    fun getFormattedTotalRunningTime(): String {
        return formatSeconds(totalRunningTime)
    }
    
    /**
     * 格式化秒数为可读格式
     */
    private fun formatSeconds(seconds: Long): String {
        return when {
            seconds < 60 -> "${seconds}秒"
            seconds < 3600 -> "${seconds / 60}分钟${seconds % 60}秒"
            seconds < 86400 -> "${seconds / 3600}小时${(seconds % 3600) / 60}分钟"
            else -> "${seconds / 86400}天${(seconds % 86400) / 3600}小时"
        }
    }
    
    companion object {
        /**
         * 创建空的统计信息
         */
        fun empty(): QueryStatistics {
            return QueryStatistics(
                totalTasks = 0,
                totalRunningTime = 0,
                avgGpuUsage = 0.0,
                maxGpuUsage = 0.0,
                avgGpuMemoryUsage = 0.0,
                maxGpuMemoryUsage = 0.0,
                totalGpuMemoryUsed = 0.0,
                multiGpuTaskCount = 0,
                debugModeTaskCount = 0,
                avgRunningTime = 0.0,
                userDistribution = emptyMap(),
                projectDistribution = emptyMap(),
                deviceDistribution = emptyMap(),
                taskTypeDistribution = emptyMap(),
                earliestStartTime = null,
                latestFinishTime = null
            )
        }
    }
}