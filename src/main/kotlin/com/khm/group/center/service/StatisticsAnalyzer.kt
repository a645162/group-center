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
                    successTasks = 0,
                    failedTasks = 0,
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

                if (task.taskStatus.equals("success", ignoreCase = true)) {
                    user.successTasks++
                } else {
                    user.failedTasks++
                }

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
    fun generate24HourReport(tasks: List<GpuTaskInfoModel>, startTimestamp: Long, endTimestamp: Long): DailyReport {
        val userStats = analyzeUserStatistics(tasks, startTimestamp, endTimestamp)
        val gpuStats = analyzeGpuStatistics(tasks, startTimestamp, endTimestamp)
        val projectStats = analyzeProjectStatistics(tasks, startTimestamp, endTimestamp)
        val serverStats = analyzeServerStatistics(tasks, startTimestamp, endTimestamp)

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

        // 调试日志：输出实际任务时间范围
        val actualTaskStartTime = if (tasks.isNotEmpty()) {
            val minStartTime = tasks.minOf { it.taskStartTime }
            val minStartDateTime = DateTimeUtils.convertTimestampToDateTime(minStartTime)
            println("DEBUG: 实际任务最早启动时间: ${minStartDateTime} (时间戳: $minStartTime)")
            minStartDateTime
        } else {
            startTime
        }

        val actualTaskEndTime = if (tasks.isNotEmpty()) {
            val maxEndTime = tasks.maxOf { it.taskFinishTime }
            val maxEndDateTime = DateTimeUtils.convertTimestampToDateTime(maxEndTime)
            println("DEBUG: 实际任务最晚结束时间: ${maxEndDateTime} (时间戳: $maxEndTime)")
            maxEndDateTime
        } else {
            endTime
        }

        println("DEBUG: 统计区间: $startTime - $endTime")
        println("DEBUG: 实际任务时间: $actualTaskStartTime - $actualTaskEndTime")

        // 根据开始时间确定报告日期（使用自然日，即开始时间对应的日期）
        val reportDate = DateTimeUtils.convertTimestampToDateTime(startTimestamp).toLocalDate()
        
        return DailyReport(
            date = reportDate, // 使用根据时间范围确定的自然日
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
            serverStats = serverStats,
            successRate = if (actualTasks > 0) {
                tasks.count { it.taskStatus.equals("success", ignoreCase = true) } * 100.0 / actualTasks
            } else 0.0
        )
    }

    /**
     * 生成日报（按自然日统计，整点时间范围：昨天0:00到今天0:00）
     */
    fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate): DailyReport {
        // 计算自然日的开始和结束时间（秒）- 使用整点时间
        val periodStart = LocalDateTime.of(date, java.time.LocalTime.MIN)
            .atZone(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = LocalDateTime.of(date.plusDays(1), java.time.LocalTime.MIN)
            .atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)
        val serverStats = analyzeServerStatistics(tasks, periodStart, periodEnd)

        // 计算开始时间和结束时间（使用统一的转换函数）
        val startTime = if (tasks.isNotEmpty()) {
            tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
        } else {
            LocalDateTime.of(date, java.time.LocalTime.MIN)
        }

        val endTime = if (tasks.isNotEmpty()) {
            tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
        } else {
            LocalDateTime.of(date.plusDays(1), java.time.LocalTime.MIN)
        }

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        return DailyReport(
            date = date,
            startTime = DateTimeUtils.convertTimestampToDateTime(periodStart),
            endTime = DateTimeUtils.convertTimestampToDateTime(periodEnd),
            actualTaskStartTime = if (tasks.isNotEmpty()) {
                tasks.minOf { DateTimeUtils.convertTimestampToDateTime(it.taskStartTime) }
            } else {
                LocalDateTime.of(date, java.time.LocalTime.MIN)
            },
            actualTaskEndTime = if (tasks.isNotEmpty()) {
                tasks.maxOf { DateTimeUtils.convertTimestampToDateTime(it.taskFinishTime) }
            } else {
                LocalDateTime.of(date.plusDays(1), java.time.LocalTime.MIN)
            },
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(5),
            topGpus = gpuStats.take(5),
            topProjects = projectStats.take(5),
            serverStats = serverStats,
            successRate = if (actualTasks > 0) {
                tasks.count { it.taskStatus.equals("success", ignoreCase = true) } * 100.0 / actualTasks
            } else 0.0
        )
    }

    /**
     * 生成周报
     */
    fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>): WeeklyReport {
        // 计算上周的开始和结束时间（秒）
        val lastWeekStart = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.MONDAY)
        val lastWeekEnd = LocalDate.now().minusWeeks(1).with(java.time.DayOfWeek.SUNDAY)
        val periodStart = lastWeekStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = lastWeekEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)
        val timeTrend = analyzeTimeTrendStatistics(tasks, TimePeriod.ONE_WEEK)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }
        return WeeklyReport(
            startDate = LocalDate.now().minusDays(7),
            endDate = LocalDate.now(),
            periodStartDate = lastWeekStart,
            periodEndDate = lastWeekEnd,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(10),
            topGpus = gpuStats.take(10),
            topProjects = projectStats.take(10),
            dailyTrend = timeTrend.dailyStats,
            averageDailyTasks = if (actualTasks > 0) (actualTasks / 7).toInt() else 0,
            averageDailyRuntime = if (actualTasks > 0) (totalActualRuntime / 7).toInt() else 0
        )
    }

    /**
     * 生成月报
     */
    fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>): MonthlyReport {
        // 计算上月的开始和结束时间（秒）
        val lastMonth = LocalDate.now().minusMonths(1)
        val lastMonthStart = lastMonth.withDayOfMonth(1)
        val lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
        val periodStart = lastMonthStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = lastMonthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)
        val timeTrend = analyzeTimeTrendStatistics(tasks, TimePeriod.ONE_MONTH)

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        return MonthlyReport(
            month = lastMonth.month,
            year = lastMonth.year,
            periodStartDate = lastMonthStart,
            periodEndDate = lastMonthEnd,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
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
            averageWeeklyTasks = if (actualTasks > 0) (actualTasks / 4).toInt() else 0,
            averageWeeklyRuntime = if (actualTasks > 0) (totalActualRuntime / 4).toInt() else 0
        )
    }

    /**
     * 生成年报
     */
    fun generateYearlyReport(tasks: List<GpuTaskInfoModel>): YearlyReport {
        // 计算去年的开始和结束时间（秒）
        val lastYear = LocalDate.now().minusYears(1)
        val lastYearStart = LocalDate.of(lastYear.year, 1, 1)
        val lastYearEnd = LocalDate.of(lastYear.year, 12, 31)
        val periodStart = lastYearStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val periodEnd = lastYearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        val userStats = analyzeUserStatistics(tasks, periodStart, periodEnd)
        val gpuStats = analyzeGpuStatistics(tasks, periodStart, periodEnd)
        val projectStats = analyzeProjectStatistics(tasks, periodStart, periodEnd)
        val monthlyStats = analyzeTimeTrendStatistics(tasks, TimePeriod.ONE_YEAR).dailyStats
            .groupBy { it.date.month }
            .map { (month, dailyStats) ->
                MonthlyStats(
                    month = month,
                    totalTasks = dailyStats.sumOf { it.totalTasks },
                    totalRuntime = dailyStats.sumOf { it.totalRuntime },
                    activeUsers = dailyStats.flatMap { it.activeUsers }.distinct().size,
                    averageDailyTasks = if (dailyStats.isNotEmpty()) {
                        NumberFormat.formatAverage(dailyStats.sumOf { it.totalTasks }.toDouble(), dailyStats.size).toInt()
                    } else {
                        0
                    },
                    averageDailyRuntime = if (dailyStats.isNotEmpty()) {
                        NumberFormat.formatAverage(dailyStats.sumOf { it.totalRuntime }.toDouble(), dailyStats.size).toInt()
                    } else {
                        0
                    }
                )
            }

        // 计算实际的总运行时间（考虑时间重叠）
        val totalActualRuntime = tasks.sumOf { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd).toInt()
        }

        // 计算实际的任务数（有实际运行时间的任务）
        val actualTasks = tasks.count { task ->
            calculateActualRuntimeInPeriod(task, periodStart, periodEnd) > 0L
        }

        return YearlyReport(
            year = lastYear.year,
            periodStartDate = lastYearStart,
            periodEndDate = lastYearEnd,
            totalTasks = actualTasks,
            totalRuntime = totalActualRuntime,
            activeUsers = tasks.map { it.taskUser }.distinct().size,
            topUsers = userStats.take(20),
            topGpus = gpuStats.take(20),
            topProjects = projectStats.take(15),
            monthlyStats = monthlyStats.sortedBy { it.month.value },
            averageMonthlyTasks = if (actualTasks > 0) {
                NumberFormat.formatAverage(actualTasks.toDouble(), 12).toInt()
            } else {
                0
            },
            averageMonthlyRuntime = if (actualTasks > 0) {
                NumberFormat.formatAverage(totalActualRuntime.toDouble(), 12).toInt()
            } else {
                0
            }
        )
    }
}