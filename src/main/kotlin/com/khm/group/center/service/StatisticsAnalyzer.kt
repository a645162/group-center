package com.khm.group.center.service

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.time.DateTimeUtils
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class StatisticsAnalyzer {
    /**
     * 过滤多卡任务（如果启用多卡过滤）
     */
    private fun filterMultiGpuTasks(tasks: List<GpuTaskInfoModel>): List<GpuTaskInfoModel> {
        if (!ConfigEnvironment.FILTER_MULTI_GPU_TASKS) {
            return tasks
        }

        return tasks.filter { task ->
            // 如果multiDeviceWorldSize > 1 且 multiDeviceLocalRank == 0，则保留
            // 如果multiDeviceWorldSize <= 1，则保留所有任务
            task.multiDeviceWorldSize <= 1 || (task.multiDeviceWorldSize > 1 && task.multiDeviceLocalRank == 0)
        }
    }

    /**
     * 分析用户统计数据
     */
    fun analyzeUserStatistics(tasks: List<GpuTaskInfoModel>): List<UserStatistics> {
        val filteredTasks = filterMultiGpuTasks(tasks)
        val userMap = mutableMapOf<String, UserStatistics>()

        for (task in filteredTasks) {
            val userName = task.taskUser
            val user = userMap.getOrPut(userName) {
                UserStatistics(
                    userName = userName,
                    totalTasks = 0,
                    totalRuntime = 0,
                    successTasks = 0,
                    failedTasks = 0,
                    averageRuntime = 0.0,
                    favoriteGpu = "",
                    favoriteProject = ""
                )
            }

            user.totalTasks++
            user.totalRuntime += task.taskRunningTimeInSeconds

            if (task.taskStatus.equals("success", ignoreCase = true)) {
                user.successTasks++
            } else {
                user.failedTasks++
            }

            // 更新最喜欢的GPU
            if (user.favoriteGpu.isEmpty() || task.taskRunningTimeInSeconds > user.totalRuntime / user.totalTasks) {
                user.favoriteGpu = task.taskGpuName
            }

            // 更新最喜欢的项目
            if (user.favoriteProject.isEmpty() || task.projectName.isNotBlank()) {
                user.favoriteProject = task.projectName
            }
        }

        // 计算平均运行时间
        userMap.values.forEach { user ->
            user.averageRuntime = if (user.totalTasks > 0) user.totalRuntime.toDouble() / user.totalTasks else 0.0
        }

        return userMap.values.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析GPU统计数据
     */
    fun analyzeGpuStatistics(tasks: List<GpuTaskInfoModel>): List<GpuStatistics> {
        val filteredTasks = filterMultiGpuTasks(tasks)
        val gpuMap = mutableMapOf<String, GpuStatistics>()

        for (task in filteredTasks) {
            val gpuKey = "${task.taskGpuName}_${task.serverName}"
            val gpu = gpuMap.getOrPut(gpuKey) {
                GpuStatistics(
                    gpuName = task.taskGpuName,
                    serverName = task.serverName,
                    totalUsageCount = 0,
                    totalRuntime = 0,
                    averageUsagePercent = 0.0,
                    averageMemoryUsage = 0.0,
                    totalMemoryUsage = 0.0
                )
            }

            gpu.totalUsageCount++
            gpu.totalRuntime += task.taskRunningTimeInSeconds
            gpu.averageUsagePercent =
                (gpu.averageUsagePercent * (gpu.totalUsageCount - 1) + task.gpuUsagePercent) / gpu.totalUsageCount
            gpu.averageMemoryUsage =
                (gpu.averageMemoryUsage * (gpu.totalUsageCount - 1) + task.gpuMemoryPercent) / gpu.totalUsageCount
            gpu.totalMemoryUsage += task.taskGpuMemoryGb
        }

        return gpuMap.values.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析服务器统计数据
     */
    fun analyzeServerStatistics(tasks: List<GpuTaskInfoModel>): List<ServerStatistics> {
        val filteredTasks = filterMultiGpuTasks(tasks)
        val serverMap = mutableMapOf<String, ServerStatistics>()

        for (task in filteredTasks) {
            val serverName = task.serverName
            val server = serverMap.getOrPut(serverName) {
                ServerStatistics(
                    serverName = serverName,
                    totalTasks = 0,
                    totalRuntime = 0,
                    activeUsers = mutableSetOf(),
                    gpuUtilization = 0.0
                )
            }

            server.totalTasks++
            server.totalRuntime += task.taskRunningTimeInSeconds
            server.activeUsers.add(task.taskUser)
            server.gpuUtilization =
                (server.gpuUtilization * (server.totalTasks - 1) + task.gpuUsagePercent) / server.totalTasks
        }

        return serverMap.values.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析项目统计数据
     */
    fun analyzeProjectStatistics(tasks: List<GpuTaskInfoModel>): List<ProjectStatistics> {
        val filteredTasks = filterMultiGpuTasks(tasks)
        val projectMap = mutableMapOf<String, ProjectStatistics>()

        for (task in filteredTasks) {
            val projectName = task.projectName.takeIf { it.isNotBlank() } ?: "Unknown"
            val project = projectMap.getOrPut(projectName) {
                ProjectStatistics(
                    projectName = projectName,
                    totalRuntime = 0,
                    totalTasks = 0,
                    activeUsers = mutableSetOf(),
                    averageRuntime = 0.0
                )
            }

            project.totalRuntime += task.taskRunningTimeInSeconds
            project.totalTasks++
            project.activeUsers.add(task.taskUser)
        }

        projectMap.values.forEach { project ->
            project.averageRuntime =
                if (project.totalTasks > 0) project.totalRuntime.toDouble() / project.totalTasks else 0.0
        }

        return projectMap.values.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析时间趋势统计数据
     */
    fun analyzeTimeTrendStatistics(tasks: List<GpuTaskInfoModel>, timePeriod: TimePeriod): TimeTrendStatistics {
        val dailyStats = mutableMapOf<LocalDate, DailyStats>()

        for (task in tasks) {
            val taskDate = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(task.taskStartTime),
                ZoneId.systemDefault()
            ).toLocalDate()

            val dailyStat = dailyStats.getOrPut(taskDate) {
                DailyStats(
                    date = taskDate,
                    totalTasks = 0,
                    totalRuntime = 0,
                    activeUsers = mutableSetOf(),
                    peakGpuUsage = 0.0
                )
            }

            dailyStat.totalTasks++
            dailyStat.totalRuntime += task.taskRunningTimeInSeconds
            dailyStat.activeUsers.add(task.taskUser)
            dailyStat.peakGpuUsage = maxOf(dailyStat.peakGpuUsage, task.gpuUsagePercent.toDouble())
        }

        return TimeTrendStatistics(
            period = timePeriod,
            dailyStats = dailyStats.values.sortedBy { it.date },
            totalTasks = tasks.size,
            totalRuntime = tasks.sumOf { it.taskRunningTimeInSeconds },
            totalUsers = tasks.map { it.taskUser }.distinct().size,
            averageDailyTasks = if (dailyStats.isNotEmpty()) tasks.size / dailyStats.size else 0,
            averageDailyRuntime = if (dailyStats.isNotEmpty()) tasks.sumOf { it.taskRunningTimeInSeconds } / dailyStats.size else 0
        )
    }

    /**
     * 生成日报
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate): DailyReport {
        val userStats = analyzeUserStatistics(tasks)
        val gpuStats = analyzeGpuStatistics(tasks)
        val projectStats = analyzeProjectStatistics(tasks)
        val serverStats = analyzeServerStatistics(tasks)

        // 计算开始时间和结束时间（使用统一的转换函数）
        val startTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            LocalDateTime.of(date, java.time.LocalTime.MIN)
        }

        val endTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            LocalDateTime.of(date, java.time.LocalTime.MAX)
        }

        return DailyReport(
            date = date,
            startTime = startTime,
            endTime = endTime,
            totalTasks = tasks.size,
            totalRuntime = tasks.sumOf { it.taskRunningTimeInSeconds },
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(5),
            topGpus = gpuStats.take(5),
            topProjects = projectStats.take(5),
            serverStats = serverStats,
            successRate = if (tasks.isNotEmpty()) {
                tasks.count { it.taskStatus.equals("success", ignoreCase = true) } * 100.0 / tasks.size
            } else 0.0
        )
    }

    /**
     * 生成周报
     */
    fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>): WeeklyReport {
        val userStats = analyzeUserStatistics(tasks)
        val gpuStats = analyzeGpuStatistics(tasks)
        val timeTrend = analyzeTimeTrendStatistics(tasks, TimePeriod.ONE_WEEK)

        val lastWeekStart = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.MONDAY)
        val lastWeekEnd = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.SUNDAY)

        return WeeklyReport(
            startDate = LocalDate.now().minusDays(7),
            endDate = LocalDate.now(),
            periodStartDate = lastWeekStart,
            periodEndDate = lastWeekEnd,
            totalTasks = tasks.size,
            totalRuntime = tasks.sumOf { it.taskRunningTimeInSeconds },
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(10),
            topGpus = gpuStats.take(10),
            dailyTrend = timeTrend.dailyStats,
            averageDailyTasks = timeTrend.averageDailyTasks,
            averageDailyRuntime = timeTrend.averageDailyRuntime
        )
    }

    /**
     * 生成月报
     */
    fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>): MonthlyReport {
        val userStats = analyzeUserStatistics(tasks)
        val gpuStats = analyzeGpuStatistics(tasks)
        val projectStats = analyzeProjectStatistics(tasks)
        val timeTrend = analyzeTimeTrendStatistics(tasks, TimePeriod.ONE_MONTH)

        val lastMonth = LocalDate.now().minusMonths(1)
        val lastMonthStart = lastMonth.withDayOfMonth(1)
        val lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())

        return MonthlyReport(
            month = lastMonth.month,
            year = lastMonth.year,
            periodStartDate = lastMonthStart,
            periodEndDate = lastMonthEnd,
            totalTasks = tasks.size,
            totalRuntime = tasks.sumOf { it.taskRunningTimeInSeconds },
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(15),
            topGpus = gpuStats.take(15),
            topProjects = projectStats.take(10),
            weeklyTrend = timeTrend.dailyStats.chunked(7).map { weekStats ->
                WeeklyStats(
                    startDate = weekStats.first().date,
                    endDate = weekStats.last().date,
                    totalTasks = weekStats.sumOf { it.totalTasks },
                    totalRuntime = weekStats.sumOf { it.totalRuntime },
                    activeUsers = weekStats.flatMap { it.activeUsers }.distinct().size
                )
            },
            averageWeeklyTasks = if (timeTrend.dailyStats.isNotEmpty()) tasks.size / (timeTrend.dailyStats.size / 7) else 0,
            averageWeeklyRuntime = if (timeTrend.dailyStats.isNotEmpty()) tasks.sumOf { it.taskRunningTimeInSeconds } / (timeTrend.dailyStats.size / 7) else 0
        )
    }

    /**
     * 生成年报
     */
    fun generateYearlyReport(tasks: List<GpuTaskInfoModel>): YearlyReport {
        val userStats = analyzeUserStatistics(tasks)
        val gpuStats = analyzeGpuStatistics(tasks)
        val projectStats = analyzeProjectStatistics(tasks)
        val monthlyStats = analyzeTimeTrendStatistics(tasks, TimePeriod.ONE_YEAR).dailyStats
            .groupBy { it.date.month }
            .map { (month, dailyStats) ->
                MonthlyStats(
                    month = month,
                    totalTasks = dailyStats.sumOf { it.totalTasks },
                    totalRuntime = dailyStats.sumOf { it.totalRuntime },
                    activeUsers = dailyStats.flatMap { it.activeUsers }.distinct().size,
                    averageDailyTasks = if (dailyStats.isNotEmpty()) dailyStats.sumOf { it.totalTasks } / dailyStats.size else 0,
                    averageDailyRuntime = if (dailyStats.isNotEmpty()) dailyStats.sumOf { it.totalRuntime } / dailyStats.size else 0
                )
            }

        val lastYear = LocalDate.now().minusYears(1)
        val lastYearStart = LocalDate.of(lastYear.year, 1, 1)
        val lastYearEnd = LocalDate.of(lastYear.year, 12, 31)

        return YearlyReport(
            year = lastYear.year,
            periodStartDate = lastYearStart,
            periodEndDate = lastYearEnd,
            totalTasks = tasks.size,
            totalRuntime = tasks.sumOf { it.taskRunningTimeInSeconds },
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(20),
            topGpus = gpuStats.take(20),
            topProjects = projectStats.take(15),
            monthlyStats = monthlyStats.sortedBy { it.month.value },
            averageMonthlyTasks = if (monthlyStats.isNotEmpty()) tasks.size / monthlyStats.size else 0,
            averageMonthlyRuntime = if (monthlyStats.isNotEmpty()) tasks.sumOf { it.taskRunningTimeInSeconds } / monthlyStats.size else 0
        )
    }
}