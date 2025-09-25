package com.khm.group.center.datatype.statistics

import com.khm.group.center.utils.format.NumberFormat
import java.time.LocalDate
import java.time.Month

// 用户统计数据结构
data class UserStatistics(
    val userName: String,
    var totalTasks: Int,
    var totalRuntime: Int,
    var successTasks: Int,
    var failedTasks: Int,
    var averageRuntime: Double,
    var favoriteGpu: String,
    var favoriteProject: String
) {
    val successRate: Double get() = if (totalTasks > 0) successTasks * 100.0 / totalTasks else 0.0
    val formattedAverageRuntime: Double get() = NumberFormat.formatDouble(averageRuntime)
    val formattedSuccessRate: Double get() = NumberFormat.formatDouble(successRate)
}

// GPU统计数据结构
data class GpuStatistics(
    val gpuName: String,
    val serverName: String,
    var totalUsageCount: Int,
    var totalRuntime: Int,
    var averageUsagePercent: Double,
    var averageMemoryUsage: Double,
    var totalMemoryUsage: Double
) {
    val formattedAverageUsagePercent: Double get() = NumberFormat.formatDouble(averageUsagePercent)
    val formattedAverageMemoryUsage: Double get() = NumberFormat.formatDouble(averageMemoryUsage)
    val formattedTotalMemoryUsage: Double get() = NumberFormat.formatDouble(totalMemoryUsage)
}

// 服务器统计数据结构
data class ServerStatistics(
    val serverName: String,
    var totalTasks: Int,
    var totalRuntime: Int,
    val activeUsers: MutableSet<String>,
    var gpuUtilization: Double
) {
    val activeUsersCount: Int get() = activeUsers.size
    val formattedGpuUtilization: Double get() = NumberFormat.formatDouble(gpuUtilization)
}

// 项目统计数据结构
data class ProjectStatistics(
    val projectName: String,
    var totalRuntime: Int,
    var totalTasks: Int,
    val activeUsers: MutableSet<String>,
    var averageRuntime: Double
) {
    val activeUsersCount: Int get() = activeUsers.size
    val formattedAverageRuntime: Double get() = NumberFormat.formatDouble(averageRuntime)
}

// 每日统计数据结构
data class DailyStats(
    val date: LocalDate,
    var totalTasks: Int,
    var totalRuntime: Int,
    val activeUsers: MutableSet<String>,
    var peakGpuUsage: Double
) {
    val activeUsersCount: Int get() = activeUsers.size
    val formattedPeakGpuUsage: Double get() = NumberFormat.formatDouble(peakGpuUsage)
}

// 时间趋势统计数据结构
data class TimeTrendStatistics(
    val period: com.khm.group.center.utils.time.TimePeriod,
    val dailyStats: List<DailyStats>,
    val totalTasks: Int,
    val totalRuntime: Int,
    val totalUsers: Int,
    val averageDailyTasks: Int,
    val averageDailyRuntime: Int
)

// 日报数据结构
data class DailyReport(
    val date: LocalDate,
    val startTime: java.time.LocalDateTime,
    val endTime: java.time.LocalDateTime,
    val totalTasks: Int,
    val totalRuntime: Int,
    val activeUsers: Int,
    val topUsers: List<UserStatistics>,
    val topGpus: List<GpuStatistics>,
    val topProjects: List<ProjectStatistics>,
    val serverStats: List<ServerStatistics>,
    val successRate: Double,
    val refreshTime: java.time.LocalDateTime = java.time.LocalDateTime.now()
) {
    val formattedSuccessRate: Double get() = NumberFormat.formatDouble(successRate)
}
// 周报数据结构
data class WeeklyReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val totalTasks: Int,
    val totalRuntime: Int,
    val activeUsers: Int,
    val topUsers: List<UserStatistics>,
    val topGpus: List<GpuStatistics>,
    val topProjects: List<ProjectStatistics>,
    val dailyTrend: List<DailyStats>,
    val averageDailyTasks: Int,
    val averageDailyRuntime: Int,
    val refreshTime: java.time.LocalDateTime = java.time.LocalDateTime.now()
)

// 周统计数据结构
data class WeeklyStats(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalTasks: Int,
    val totalRuntime: Int,
    val activeUsers: Int
)

// 月报数据结构
data class MonthlyReport(
    val month: Month,
    val year: Int,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val totalTasks: Int,
    val totalRuntime: Int,
    val activeUsers: Int,
    val topUsers: List<UserStatistics>,
    val topGpus: List<GpuStatistics>,
    val topProjects: List<ProjectStatistics>,
    val weeklyTrend: List<WeeklyStats>,
    val averageWeeklyTasks: Int,
    val averageWeeklyRuntime: Int,
    val refreshTime: java.time.LocalDateTime = java.time.LocalDateTime.now()
)

// 月统计数据结构
data class MonthlyStats(
    val month: Month,
    val totalTasks: Int,
    val totalRuntime: Int,
    val activeUsers: Int,
    val averageDailyTasks: Int,
    val averageDailyRuntime: Int
)

// 年报数据结构
data class YearlyReport(
    val year: Int,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val totalTasks: Int,
    val totalRuntime: Int,
    val activeUsers: Int,
    val topUsers: List<UserStatistics>,
    val topGpus: List<GpuStatistics>,
    val topProjects: List<ProjectStatistics>,
    val monthlyStats: List<MonthlyStats>,
    val averageMonthlyTasks: Int,
    val averageMonthlyRuntime: Int,
    val refreshTime: java.time.LocalDateTime = java.time.LocalDateTime.now()
)