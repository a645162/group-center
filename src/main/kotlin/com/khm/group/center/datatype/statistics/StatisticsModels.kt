package com.khm.group.center.datatype.statistics

import com.khm.group.center.utils.format.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

/**
 * 报告类型枚举
 * 定义不同类型的统计报告
 */
enum class ReportType {
    TODAY,           // 今日日报
    YESTERDAY,       // 昨日日报
    WEEKLY,          // 周报（上周）
    MONTHLY,         // 月报（上月）
    YEARLY,          // 年报（去年）
    CUSTOM           // 自定义时间段报告
}

/**
 * 统一的报告数据结构
 * 替代原有的 DailyReport, WeeklyReport, MonthlyReport, YearlyReport, CustomPeriodStatistics
 */
data class Report(
    val reportType: ReportType,                    // 报告类型
    val title: String,                             // 报告标题
    val periodStartDate: LocalDate,                // 统计开始日期
    val periodEndDate: LocalDate,                  // 统计结束日期
    val startTime: LocalDateTime,                  // 统计开始时间
    val endTime: LocalDateTime,                    // 统计结束时间
    val actualTaskStartTime: LocalDateTime,        // 实际任务最早开始时间
    val actualTaskEndTime: LocalDateTime,          // 实际任务最晚结束时间
    val totalTasks: Int,                           // 总任务数
    val totalRuntime: Int,                         // 总运行时间（秒）
    val activeUsers: Int,                          // 活跃用户数
    val topUsers: List<UserStatistics>,            // Top用户列表
    val topGpus: List<GpuStatistics>,              // Top GPU列表
    val topProjects: List<ProjectStatistics>,      // Top项目列表
    val sleepAnalysis: SleepAnalysis?,             // 作息分析数据（可选）
    val refreshTime: LocalDateTime = LocalDateTime.now()  // 报告生成时间
) {
    /**
     * 获取报告的时间范围描述
     */
    fun getTimeRangeDescription(): String {
        return when (reportType) {
            ReportType.TODAY -> "今日"
            ReportType.YESTERDAY -> "昨日"
            ReportType.WEEKLY -> "上周"
            ReportType.MONTHLY -> "上月"
            ReportType.YEARLY -> "去年"
            ReportType.CUSTOM -> "自定义时间段"
        }
    }
}

// 用户统计数据结构（简化版，移除成功率相关字段）
data class UserStatistics(
    var userName: String = "",
    var totalTasks: Int = 0,
    var totalRuntime: Int = 0,
    var averageRuntime: Double = 0.0,
    var favoriteGpu: String = "",
    var favoriteProject: String = ""
) {
    val formattedAverageRuntime: Double get() = NumberFormat.formatDouble(averageRuntime)
}

// GPU统计数据结构（保持不变）
data class GpuStatistics(
    var gpuName: String = "",
    var serverName: String = "",
    var totalUsageCount: Int = 0,
    var totalRuntime: Int = 0,
    var averageUsagePercent: Double = 0.0,
    var averageMemoryUsage: Double = 0.0,
    var totalMemoryUsage: Double = 0.0
) {
    val formattedAverageUsagePercent: Double get() = NumberFormat.formatDouble(averageUsagePercent)
    val formattedAverageMemoryUsage: Double get() = NumberFormat.formatDouble(averageMemoryUsage)
    val formattedTotalMemoryUsage: Double get() = NumberFormat.formatDouble(totalMemoryUsage)
}

// 服务器统计数据结构
data class ServerStatistics(
    var serverName: String = "",
    var totalTasks: Int = 0,
    var totalRuntime: Int = 0,
    var activeUsers: MutableSet<String> = mutableSetOf(),
    var gpuUtilization: Double = 0.0
) {
    val activeUsersCount: Int get() = activeUsers.size
    val formattedGpuUtilization: Double get() = NumberFormat.formatDouble(gpuUtilization)
}

// 项目统计数据结构（保持不变）
data class ProjectStatistics(
    var projectName: String = "",
    var totalRuntime: Int = 0,
    var totalTasks: Int = 0,
    var activeUsers: MutableSet<String> = mutableSetOf(),
    var averageRuntime: Double = 0.0
) {
    val activeUsersCount: Int get() = activeUsers.size
    val formattedAverageRuntime: Double get() = NumberFormat.formatDouble(averageRuntime)
}

// 作息分析数据结构（保持不变）
data class SleepAnalysis(
    val lateNightTasks: List<com.khm.group.center.db.model.client.GpuTaskInfoModel>,
    val earlyMorningTasks: List<com.khm.group.center.db.model.client.GpuTaskInfoModel>,
    val lateNightChampion: com.khm.group.center.db.model.client.GpuTaskInfoModel?, // 熬夜冠军（最晚启动）
    val earlyMorningChampion: com.khm.group.center.db.model.client.GpuTaskInfoModel?, // 早起冠军（最早启动）
    val totalLateNightTasks: Int,
    val totalEarlyMorningTasks: Int,
    val lateNightUsers: Set<String>, // 熬夜用户集合
    val earlyMorningUsers: Set<String>, // 早起用户集合
    val refreshTime: LocalDateTime = LocalDateTime.now()
) {
    val totalLateNightUsers: Int get() = lateNightUsers.size
    val totalEarlyMorningUsers: Int get() = earlyMorningUsers.size
}

// 以下为保留的辅助数据结构，用于趋势分析等场景
data class DailyStats(
    var date: LocalDate = LocalDate.now(),
    var totalTasks: Int = 0,
    var totalRuntime: Int = 0,
    var activeUsers: MutableSet<String> = mutableSetOf(),
    var peakGpuUsage: Double = 0.0
) {
    val activeUsersCount: Int get() = activeUsers.size
    val formattedPeakGpuUsage: Double get() = NumberFormat.formatDouble(peakGpuUsage)
}

data class TimeTrendStatistics(
    var period: com.khm.group.center.utils.time.TimePeriod = com.khm.group.center.utils.time.TimePeriod.ONE_WEEK,
    var dailyStats: List<DailyStats> = emptyList(),
    var totalTasks: Int = 0,
    var totalRuntime: Int = 0,
    var totalUsers: Int = 0,
    var averageDailyTasks: Int = 0,
    var averageDailyRuntime: Int = 0
)

/**
 * 用户活动时间区间
 * 用于统计用户的活动时间段分布
 */
data class UserActivityTimeRange(
    var userName: String = "",
    var earliestStartTime: LocalDateTime? = null,  // 最早启动时间
    var latestStartTime: LocalDateTime? = null,    // 最晚启动时间
    var activityTimeRange: String = "",            // 活动时间区间字符串
    var totalTasks: Int = 0,                       // 总任务数
    var totalRuntime: Int = 0,                     // 总运行时间
    var isCrossDayActivity: Boolean = false,       // 是否为跨天活动
    var crossDayActivityRange: String = ""         // 跨天活动区间字符串
)

/**
 * 用户活动时间分布响应
 * 包含所有用户的活动时间区间信息
 */
data class UserActivityTimeDistribution(
    var users: List<UserActivityTimeRange> = emptyList(),
    var totalUsers: Int = 0,
    var refreshTime: LocalDateTime = LocalDateTime.now()
)