package com.khm.group.center.service

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.format.NumberFormat
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.time.DateTimeUtils
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
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
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(date, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
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
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(lastWeekStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
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
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(lastMonthStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
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
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(lastYearStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
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
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            DateTimeUtils.convertTimestampToDateTime(startTime)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
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
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
            sleepAnalysis = null
        )
    }

    /**
     * 生成指定日期范围的日报（无缓存）
     * @param tasks 任务列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, startDate: LocalDate, endDate: LocalDate): Report {
        // 计算日期范围的开始和结束时间（秒）
        val periodStart = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(startDate, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
        } else {
            LocalDateTime.of(endDate, java.time.LocalTime.MAX)
        }

        // 确定报告类型和标题
        val reportType = if (startDate == endDate) {
            if (startDate == LocalDate.now()) ReportType.TODAY else ReportType.YESTERDAY
        } else {
            ReportType.CUSTOM
        }

        val title = if (startDate == endDate) {
            if (startDate == LocalDate.now()) "GPU使用日报 - 今日" else "GPU使用日报 - ${startDate}"
        } else {
            "GPU使用日报 - ${startDate} 至 ${endDate}"
        }

        return Report(
            reportType = reportType,
            title = title,
            periodStartDate = startDate,
            periodEndDate = endDate,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
            sleepAnalysis = null
        )
    }

    /**
     * 生成指定年份和月份的月报（无缓存）
     * @param tasks 任务列表
     * @param year 年份
     * @param month 月份
     * @return 报告
     */
    fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>, year: Int, month: Int): Report {
        // 计算指定月份的开始和结束时间（秒）
        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth())
        val periodStart = monthStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = monthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(monthStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
        } else {
            LocalDateTime.of(monthEnd, java.time.LocalTime.MAX)
        }

        return Report(
            reportType = ReportType.MONTHLY,
            title = "GPU使用月报 - ${year}年${month}月",
            periodStartDate = monthStart,
            periodEndDate = monthEnd,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
            sleepAnalysis = null
        )
    }

    /**
     * 生成指定年份和周的周报（无缓存）
     * @param tasks 任务列表
     * @param year 年份
     * @param week 周数
     * @return 报告
     */
    fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>, year: Int, week: Int): Report {
        // 计算指定周的开始和结束时间（秒）
        val weekStart = LocalDate.of(year, 1, 1)
            .with(java.time.temporal.WeekFields.ISO.weekOfYear(), week.toLong())
            .with(java.time.DayOfWeek.MONDAY)
        val weekEnd = weekStart.with(java.time.DayOfWeek.SUNDAY)
        val periodStart = weekStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = weekEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(weekStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
        } else {
            LocalDateTime.of(weekEnd, java.time.LocalTime.MAX)
        }

        return Report(
            reportType = ReportType.WEEKLY,
            title = "GPU使用周报 - ${year}年第${week}周",
            periodStartDate = weekStart,
            periodEndDate = weekEnd,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
            sleepAnalysis = null
        )
    }

    /**
     * 生成指定年份的年报（无缓存）
     * @param tasks 任务列表
     * @param year 年份
     * @return 报告
     */
    fun generateYearlyReport(tasks: List<GpuTaskInfoModel>, year: Int): Report {
        // 计算指定年份的开始和结束时间（秒）
        val yearStart = LocalDate.of(year, 1, 1)
        val yearEnd = LocalDate.of(year, 12, 31)
        val periodStart = yearStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = yearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

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

        // 计算实际任务时间范围 - 取所有任务的最早开始时间和最晚结束时间
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            DateTimeUtils.convertTimestampToDateTime(minStartTime)
        } else {
            LocalDateTime.of(yearStart, java.time.LocalTime.MIN)
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            DateTimeUtils.convertTimestampToDateTime(maxEndTime)
        } else {
            LocalDateTime.of(yearEnd, java.time.LocalTime.MAX)
        }

        return Report(
            reportType = ReportType.YEARLY,
            title = "GPU使用年报 - ${year}年",
            periodStartDate = yearStart,
            periodEndDate = yearEnd,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = actualTaskStartTime,
            actualTaskEndTime = actualTaskEndTime,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats,
            topGpus = gpuStats,
            topProjects = projectStats,
            sleepAnalysis = null
        )
    }

    /**
     * 分析用户活动时间分布（使用新的活动区间计算算法）
     * @param tasks 任务列表
     * @param periodStart 开始时间（秒，可选）
     * @param periodEnd 结束时间（秒，可选）
     * @return 用户活动时间分布
     */
    fun analyzeUserActivityTimeDistribution(
        tasks: List<GpuTaskInfoModel>,
        periodStart: Long? = null,
        periodEnd: Long? = null
    ): UserActivityTimeDistribution {
        val filteredTasks = filterMultiGpuTasks(tasks)
        val userMap = mutableMapOf<String, UserActivityTimeRange>()

        // 按用户分组任务
        val tasksByUser = filteredTasks.groupBy { it.taskUser }

        for ((userName, userTasks) in tasksByUser) {
            // 使用活动区间计算算法
            val activityRange = calculateActivityRange(userTasks)
            
            val user = UserActivityTimeRange(
                userName = userName,
                earliestStartTime = activityRange.earliest,
                latestStartTime = activityRange.latest,
                activityTimeRange = activityRange.rangeString,
                totalTasks = userTasks.size,
                totalRuntime = userTasks.sumOf { it.taskRunningTimeInSeconds },
                isCrossDayActivity = activityRange.isCrossDay,
                crossDayActivityRange = activityRange.crossDayRange,
                isSinglePointActivity = activityRange.isSinglePoint,
                dailyRangesCount = activityRange.dailyRangesCount,
                hasLateNightActivity = activityRange.hasLateNightActivity,
                hasEarlyMorningActivity = activityRange.hasEarlyMorningActivity,
                hasDaytimeActivity = activityRange.hasDaytimeActivity
            )
            
            userMap[userName] = user
        }

        val userList = userMap.values.filter { it.totalRuntime > 0 }.sortedByDescending { it.totalRuntime }

        return UserActivityTimeDistribution(
            users = userList,
            totalUsers = userList.size,
            refreshTime = LocalDateTime.now()
        )
    }

    /**
     * 活动区间计算算法（使用4点分界线的时间归一化算法）
     * 步骤：所有时间-4小时 → 归一化到同一天 → 计算区间 → 区间端点+4小时
     * @param userTasks 用户的任务列表
     * @return 活动时间区间
     */
    private fun calculateActivityRange(userTasks: List<GpuTaskInfoModel>): ActivityRangeResult {
        if (userTasks.isEmpty()) {
            return ActivityRangeResult.EMPTY
        }

        // 步骤1：获取所有原始时间并减去4小时（4点分界线）
        val originalTimes = userTasks.map { task ->
            DateTimeUtils.convertTimestampToDateTime(task.taskStartTime)
        }
        val adjustedTimes = originalTimes.map { it.minusHours(4) }

        // 步骤2：时间归一化 - 将所有调整后的时间映射到同一天（1970-01-01）
        val normalizedTimes = adjustedTimes.map { adjustedTime ->
            // 保留小时和分钟，但日期设为固定值
            LocalDateTime.of(1970, 1, 1, adjustedTime.hour, adjustedTime.minute, adjustedTime.second)
        }

        // 步骤3：计算归一化后的最小值和最大值
        val minNormalized = normalizedTimes.minOrNull()!!
        val maxNormalized = normalizedTimes.maxOrNull()!!

        // 步骤4：将归一化时间还原为调整后的时间（只保留时间部分）
        val earliestAdjusted = minNormalized
        val latestAdjusted = maxNormalized

        // 步骤5：区间端点加回4小时
        val earliest = earliestAdjusted.plusHours(4)
        val latest = latestAdjusted.plusHours(4)

        // 步骤6：获取原始的最早和最晚时间用于显示
        val earliestOriginal = originalTimes.minOrNull()!!
        val latestOriginal = originalTimes.maxOrNull()!!

        // 计算活动模式统计
        val activityStats = calculateActivityStatistics(userTasks)

        // 格式化时间区间
        val (rangeString, isCrossDay, crossDayRange) = formatActivityRange(earliest, latest)

        return ActivityRangeResult(
            earliest = earliestOriginal,  // 使用原始最早时间用于显示
            latest = latestOriginal,      // 使用原始最晚时间用于显示
            rangeString = rangeString,
            isCrossDay = isCrossDay,
            crossDayRange = crossDayRange,
            isSinglePoint = minNormalized == maxNormalized,
            dailyRangesCount = activityStats.dailyRangesCount,
            hasLateNightActivity = activityStats.hasLateNightActivity,
            hasEarlyMorningActivity = activityStats.hasEarlyMorningActivity,
            hasDaytimeActivity = activityStats.hasDaytimeActivity
        )
    }

    /**
     * 计算活动模式统计
     */
    private fun calculateActivityStatistics(userTasks: List<GpuTaskInfoModel>): ActivityStats {
        val dailyTasks = mutableMapOf<LocalDate, MutableList<GpuTaskInfoModel>>()
        var hasLateNight = false
        var hasEarlyMorning = false
        var hasDaytime = false

        for (task in userTasks) {
            val startTime = DateTimeUtils.convertTimestampToDateTime(task.taskStartTime)
            
            // 统计活动时间段
            when (startTime.hour) {
                in 0..3 -> hasLateNight = true      // 0-3点：凌晨
                in 4..9 -> hasEarlyMorning = true   // 4-9点：早起
                in 10..23 -> hasDaytime = true      // 10-23点：白天
            }

            // 按自然日分组统计天数
            val dayKey = startTime.toLocalDate()
            dailyTasks.getOrPut(dayKey) { mutableListOf() }.add(task)
        }

        return ActivityStats(
            dailyRangesCount = dailyTasks.size,
            hasLateNightActivity = hasLateNight,
            hasEarlyMorningActivity = hasEarlyMorning,
            hasDaytimeActivity = hasDaytime
        )
    }

    /**
     * 格式化活动时间区间
     */
    private fun formatActivityRange(earliest: LocalDateTime, latest: LocalDateTime): Triple<String, Boolean, String> {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val startStr = earliest.format(timeFormatter)
        val endStr = latest.format(timeFormatter)

        // 检查是否为跨天活动
        // 关键判断：左端点大于右端点（即22:00-2:00这种熬夜情况）
        val isCrossDay = earliest > latest

        return if (isCrossDay) {
            // Debug output earliest and latest
            logger.info("Debug: Earliest = $earliest, Latest = $latest")

            Triple(
                "$startStr-次日$endStr",
                true,
                "$startStr → 次日 $endStr"
            )
        } else {
            Triple(
                "$startStr-$endStr",
                false,
                ""
            )
        }
    }

    /**
     * 活动区间结果
     */
    private data class ActivityRangeResult(
        val earliest: LocalDateTime?,
        val latest: LocalDateTime?,
        val rangeString: String,
        val isCrossDay: Boolean,
        val crossDayRange: String,
        val isSinglePoint: Boolean = false,
        val dailyRangesCount: Int = 0,
        val hasLateNightActivity: Boolean = false,
        val hasEarlyMorningActivity: Boolean = false,
        val hasDaytimeActivity: Boolean = false
    ) {
        companion object {
            val EMPTY = ActivityRangeResult(
                earliest = null,
                latest = null,
                rangeString = "",
                isCrossDay = false,
                crossDayRange = "",
                isSinglePoint = true,
                dailyRangesCount = 0,
                hasLateNightActivity = false,
                hasEarlyMorningActivity = false,
                hasDaytimeActivity = false
            )
        }
    }

    /**
     * 活动统计信息
     */
    private data class ActivityStats(
        val dailyRangesCount: Int,
        val hasLateNightActivity: Boolean,
        val hasEarlyMorningActivity: Boolean,
        val hasDaytimeActivity: Boolean
    )
}