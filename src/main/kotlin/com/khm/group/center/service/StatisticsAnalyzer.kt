package com.khm.group.center.service

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.format.NumberFormat
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
     * 计算任务在统计区间内的实际运行时间
     * @param task 任务信息
     * @param periodStart 统计区间开始时间（秒）
     * @param periodEnd 统计区间结束时间（秒）
     * @return 任务在统计区间内的实际运行时间（秒）
     */
    private fun calculateActualRuntimeInPeriod(task: GpuTaskInfoModel, periodStart: Long, periodEnd: Long): Long {
        // 计算任务与统计区间的时间重叠
        val actualStart = maxOf(task.taskStartTime, periodStart)
        val actualEnd = minOf(task.taskFinishTime, periodEnd)
        
        // 确保时间重叠有效
        return if (actualStart <= actualEnd) {
            actualEnd - actualStart
        } else {
            0L
        }
    }

    /**
     * 分析用户统计数据
     */
    fun analyzeUserStatistics(tasks: List<GpuTaskInfoModel>, periodStart: Long? = null, periodEnd: Long? = null): List<UserStatistics> {
        val filteredTasks = filterMultiGpuTasks(tasks)
        val userMap = mutableMapOf<String, UserStatistics>()

        for (task in filteredTasks) {
            val userName = task.taskUser
            val user = userMap.getOrPut(userName) {
                UserStatistics(
                    userName = userName,
                    totalTasks = 0,
                    totalRuntime = 0,
                    averageRuntime = 0.0,
                    favoriteGpu = "",
                    favoriteProject = ""
                )
            }

            // 计算任务在统计区间内的实际运行时间
            val actualRuntime: Long = if (periodStart != null && periodEnd != null) {
                calculateActualRuntimeInPeriod(task, periodStart, periodEnd)
            } else {
                task.taskRunningTimeInSeconds.toLong()
            }

            // 只有当任务在统计区间内有实际运行时间时才计入统计
            if (actualRuntime > 0L) {
                user.totalTasks++
                user.totalRuntime += actualRuntime.toInt()

                // 更新最喜欢的GPU（基于实际运行时间）
                if (user.favoriteGpu.isEmpty() || actualRuntime.toDouble() > user.totalRuntime.toDouble() / user.totalTasks) {
                    user.favoriteGpu = task.taskGpuName
                }

                // 更新最喜欢的项目
                if (user.favoriteProject.isEmpty() || task.projectName.isNotBlank()) {
                    user.favoriteProject = task.projectName
                }
            }
        }

        // 计算平均运行时间
        userMap.values.forEach { user ->
            user.averageRuntime = if (user.totalTasks > 0) {
                NumberFormat.formatAverage(user.totalRuntime.toDouble(), user.totalTasks)
            } else {
                0.0
            }
        }

        return userMap.values.filter { it.totalRuntime > 0 }.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析GPU统计数据
     */
    fun analyzeGpuStatistics(tasks: List<GpuTaskInfoModel>, periodStart: Long? = null, periodEnd: Long? = null): List<GpuStatistics> {
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

            // 计算任务在统计区间内的实际运行时间
            val actualRuntime: Long = if (periodStart != null && periodEnd != null) {
                calculateActualRuntimeInPeriod(task, periodStart, periodEnd)
            } else {
                task.taskRunningTimeInSeconds.toLong()
            }

            // 只有当任务在统计区间内有实际运行时间时才计入统计
            if (actualRuntime > 0L) {
                gpu.totalUsageCount++
                gpu.totalRuntime += actualRuntime.toInt()
                gpu.averageUsagePercent = NumberFormat.formatWeightedAverage(
                    gpu.averageUsagePercent,
                    gpu.totalUsageCount - 1,
                    task.gpuUsagePercent.toDouble()
                )
                gpu.averageMemoryUsage = NumberFormat.formatWeightedAverage(
                    gpu.averageMemoryUsage,
                    gpu.totalUsageCount - 1,
                    task.gpuMemoryPercent.toDouble()
                )
                gpu.totalMemoryUsage += task.taskGpuMemoryGb
            }
        }

        return gpuMap.values.filter { it.totalRuntime > 0 }.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析服务器统计数据
     */
    fun analyzeServerStatistics(tasks: List<GpuTaskInfoModel>, periodStart: Long? = null, periodEnd: Long? = null): List<ServerStatistics> {
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

            // 计算任务在统计区间内的实际运行时间
            val actualRuntime: Long = if (periodStart != null && periodEnd != null) {
                calculateActualRuntimeInPeriod(task, periodStart, periodEnd)
            } else {
                task.taskRunningTimeInSeconds.toLong()
            }

            // 只有当任务在统计区间内有实际运行时间时才计入统计
            if (actualRuntime > 0L) {
                server.totalTasks++
                server.totalRuntime += actualRuntime.toInt()
                server.activeUsers.add(task.taskUser)
                server.gpuUtilization = NumberFormat.formatWeightedAverage(
                    server.gpuUtilization,
                    server.totalTasks - 1,
                    task.gpuUsagePercent.toDouble()
                )
            }
        }

        return serverMap.values.filter { it.totalRuntime > 0 }.sortedByDescending { it.totalRuntime }
    }

    /**
     * 分析项目统计数据
     */
    fun analyzeProjectStatistics(tasks: List<GpuTaskInfoModel>, periodStart: Long? = null, periodEnd: Long? = null): List<ProjectStatistics> {
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

            // 计算任务在统计区间内的实际运行时间
            val actualRuntime: Long = if (periodStart != null && periodEnd != null) {
                calculateActualRuntimeInPeriod(task, periodStart, periodEnd)
            } else {
                task.taskRunningTimeInSeconds.toLong()
            }

            // 只有当任务在统计区间内有实际运行时间时才计入统计
            if (actualRuntime > 0L) {
                project.totalRuntime += actualRuntime.toInt()
                project.totalTasks++
                project.activeUsers.add(task.taskUser)
            }
        }

        projectMap.values.forEach { project ->
            project.averageRuntime = if (project.totalTasks > 0) {
                NumberFormat.formatAverage(project.totalRuntime.toDouble(), project.totalTasks)
            } else {
                0.0
            }
        }

        return projectMap.values.filter { it.totalRuntime > 0 }.sortedByDescending { it.totalRuntime }
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
            dailyStat.peakGpuUsage = maxOf(dailyStat.peakGpuUsage, NumberFormat.formatDouble(task.gpuUsagePercent.toDouble()))
        }

        return TimeTrendStatistics(
            period = timePeriod,
            dailyStats = dailyStats.values.sortedBy { it.date },
            totalTasks = tasks.size,
            totalRuntime = tasks.sumOf { it.taskRunningTimeInSeconds },
            totalUsers = tasks.map { it.taskUser }.distinct().size,
            averageDailyTasks = if (dailyStats.isNotEmpty()) (tasks.size / dailyStats.size).toInt() else 0,
            averageDailyRuntime = if (dailyStats.isNotEmpty()) {
                NumberFormat.formatAverage(tasks.sumOf { it.taskRunningTimeInSeconds }.toDouble(), dailyStats.size).toInt()
            } else {
                0
            }
        )
    }

    /**
     * 生成24小时报告
     */
    fun generate24HourReport(tasks: List<GpuTaskInfoModel>, startTimestamp: Long, endTimestamp: Long): Report {
        val userStats = analyzeUserStatistics(tasks, startTimestamp, endTimestamp)
        val gpuStats = analyzeGpuStatistics(tasks, startTimestamp, endTimestamp)
        val projectStats = analyzeProjectStatistics(tasks, startTimestamp, endTimestamp)

        // 计算开始时间和结束时间
        val startTime = DateTimeUtils.convertTimestampToDateTime(startTimestamp)
        val endTime = DateTimeUtils.convertTimestampToDateTime(endTimestamp)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, startTimestamp, endTimestamp).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, startTimestamp, endTimestamp) > 0L
        }

        // 计算实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            startTime
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
        } else {
            endTime
        }

        // 根据开始时间确定报告日期
        val reportDate = DateTimeUtils.convertTimestampToDateTime(startTimestamp).toLocalDate()
        
        return Report(
            reportType = ReportType.CUSTOM,
            title = "24小时报告",
            periodStartDate = reportDate,
            periodEndDate = reportDate,
            startTime = startTime,
            endTime = endTime,
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(5),
            topGpus = gpuStats.take(5),
            topProjects = projectStats.take(5),
            sleepAnalysis = null
        )
    }

    /**
     * 生成日报（按自然日统计，整点时间范围：昨天0:00到今天0:00）
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate): Report {
        // 计算自然日的开始和结束时间（秒）- 使用整点时间
        val periodStart = LocalDateTime.of(date, java.time.LocalTime.MIN)
            .atZone(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = LocalDateTime.of(date.plusDays(1), java.time.LocalTime.MIN)
            .atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)

        // 计算开始时间和结束时间
        val startTime = DateTimeUtils.convertTimestampToDateTime(periodStart)
        val endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        // 计算实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            LocalDateTime.of(date, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            LocalDateTime.of(date.plusDays(1), java.time.LocalTime.MIN)
        }

        // 确定报告类型
        val reportType = if (date == LocalDate.now()) ReportType.TODAY else ReportType.YESTERDAY
        val title = if (date == LocalDate.now()) "GPU使用日报 - 今日" else "GPU使用日报 - 昨日"

        return Report(
            reportType = reportType,
            title = title,
            periodStartDate = date,
            periodEndDate = date,
            startTime = startTime,
            endTime = endTime,
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(5),
            topGpus = gpuStats.take(5),
            topProjects = projectStats.take(5),
            sleepAnalysis = null
        )
    }

    /**
     * 生成周报
     */
    fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>): Report {
        // 计算上周的开始和结束时间（秒）
        val lastWeekStart = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.MONDAY)
        val lastWeekEnd = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.SUNDAY)
        val periodStart = lastWeekStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = lastWeekEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        // 计算实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            LocalDateTime.of(lastWeekStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            LocalDateTime.of(lastWeekEnd, java.time.LocalTime.MAX)
        }

        return Report(
            reportType = ReportType.WEEKLY,
            title = "GPU使用周报 - 上周",
            periodStartDate = lastWeekStart,
            periodEndDate = lastWeekEnd,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(10),
            topGpus = gpuStats.take(10),
            topProjects = projectStats.take(10),
            sleepAnalysis = null
        )
    }

    /**
     * 生成月报
     */
    fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>): Report {
        // 计算上月的开始和结束时间（秒）
        val lastMonth = LocalDate.now().minusMonths(1)
        val lastMonthStart = lastMonth.withDayOfMonth(1)
        val lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
        val periodStart = lastMonthStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = lastMonthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        // 计算实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            LocalDateTime.of(lastMonthStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            LocalDateTime.of(lastMonthEnd, java.time.LocalTime.MAX)
        }

        return Report(
            reportType = ReportType.MONTHLY,
            title = "GPU使用月报 - 上月",
            periodStartDate = lastMonthStart,
            periodEndDate = lastMonthEnd,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(15),
            topGpus = gpuStats.take(15),
            topProjects = projectStats.take(10),
            sleepAnalysis = null
        )
    }

    /**
     * 生成年报
     */
    fun generateYearlyReport(tasks: List<GpuTaskInfoModel>): Report {
        // 计算去年的开始和结束时间（秒）
        val lastYear = LocalDate.now().minusYears(1)
        val lastYearStart = LocalDate.of(lastYear.year, 1, 1)
        val lastYearEnd = LocalDate.of(lastYear.year, 12, 31)
        val periodStart = lastYearStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = lastYearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        // 计算实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            LocalDateTime.of(lastYearStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            LocalDateTime.of(lastYearEnd, java.time.LocalTime.MAX)
        }

        return Report(
            reportType = ReportType.YEARLY,
            title = "GPU使用年报 - 去年",
            periodStartDate = lastYearStart,
            periodEndDate = lastYearEnd,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(20),
            topGpus = gpuStats.take(20),
            topProjects = projectStats.take(15),
            sleepAnalysis = null
        )
    }

    /**
     * 生成自定义时间段报告
     */
    fun generateCustomPeriodReport(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): Report {
        val userStats = analyzeUserStatistics(tasks, startTime, endTime)
        val gpuStats = analyzeGpuStatistics(tasks, startTime, endTime)
        val projectStats = analyzeProjectStatistics(tasks, startTime, endTime)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, startTime, endTime).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, startTime, endTime) > 0L
        }

        // 计算实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            DateTimeUtils.convertTimestampToDateTime(startTime)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            DateTimeUtils.convertTimestampToDateTime(endTime)
        }

        // 转换时间格式
        val startDateTime = DateTimeUtils.convertTimestampToDateTime(startTime)
        val endDateTime = DateTimeUtils.convertTimestampToDateTime(endTime)
        val startDate = startDateTime.toLocalDate()
        val endDate = endDateTime.toLocalDate()

        return Report(
            reportType = ReportType.CUSTOM,
            title = "GPU使用报告 - 自定义时间段",
            periodStartDate = startDate,
            periodEndDate = endDate,
            startTime = startDateTime,
            endTime = endDateTime,
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(10),
            topGpus = gpuStats.take(10),
            topProjects = projectStats.take(10),
            sleepAnalysis = null
        )
    }
}