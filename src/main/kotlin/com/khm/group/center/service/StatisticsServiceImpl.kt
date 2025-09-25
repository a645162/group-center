package com.khm.group.center.service

import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.time.TimeAnalysisUtils
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 统计服务实现 - 无缓存版本
 * 实现 BaseStatisticsService 接口，专注于业务逻辑
 */
@Service
class StatisticsServiceImpl : BaseStatisticsService {

    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    @Autowired
    lateinit var statisticsAnalyzer: StatisticsAnalyzer

    @Autowired
    lateinit var timeAnalysisUtils: TimeAnalysisUtils

    override fun getUserStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long?, endTime: Long?): List<UserStatistics> {
        return statisticsAnalyzer.analyzeUserStatistics(tasks, startTime, endTime)
    }

    override fun getGpuStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long?, endTime: Long?): List<GpuStatistics> {
        return statisticsAnalyzer.analyzeGpuStatistics(tasks, startTime, endTime)
    }

    override fun getServerStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long?, endTime: Long?): List<ServerStatistics> {
        return statisticsAnalyzer.analyzeServerStatistics(tasks, startTime, endTime)
    }

    override fun getProjectStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long?, endTime: Long?): List<ProjectStatistics> {
        return statisticsAnalyzer.analyzeProjectStatistics(tasks, startTime, endTime)
    }

    override fun getTimeTrendStatistics(tasks: List<GpuTaskInfoModel>, timePeriod: TimePeriod): TimeTrendStatistics {
        return statisticsAnalyzer.analyzeTimeTrendStatistics(tasks, timePeriod)
    }

    override fun generate24HourReport(tasks: List<GpuTaskInfoModel>, startTimestamp: Long, endTimestamp: Long): DailyReport {
        return statisticsAnalyzer.generate24HourReport(tasks, startTimestamp, endTimestamp)
    }

    override fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate): DailyReport {
        return statisticsAnalyzer.generateDailyReport(tasks, date)
    }

    override fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>): WeeklyReport {
        return statisticsAnalyzer.generateWeeklyReport(tasks)
    }

    override fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>): MonthlyReport {
        return statisticsAnalyzer.generateMonthlyReport(tasks)
    }

    override fun generateYearlyReport(tasks: List<GpuTaskInfoModel>): YearlyReport {
        return statisticsAnalyzer.generateYearlyReport(tasks)
    }

    override fun getCustomPeriodStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): CustomPeriodStatistics {
        // 过滤在时间段内的任务
        val filteredTasks = tasks.filter { task ->
            task.taskStartTime >= startTime && task.taskStartTime <= endTime
        }
        
        // 计算各种统计
        val userStats = getUserStatistics(filteredTasks, startTime, endTime)
        val gpuStats = getGpuStatistics(filteredTasks, startTime, endTime)
        val serverStats = getServerStatistics(filteredTasks, startTime, endTime)
        val projectStats = getProjectStatistics(filteredTasks, startTime, endTime)
        
        // 计算作息分析
        val sleepAnalysis = getSleepAnalysis(filteredTasks, startTime, endTime)
        
        // 计算总任务数和运行时间
        val totalTasks = filteredTasks.size
        val totalRuntime = filteredTasks.sumOf { it.taskRunningTimeInSeconds }
        val activeUsers = filteredTasks.map { it.taskUser }.distinct().size
        
        // 计算成功率
        val successRate = if (totalTasks > 0) {
            filteredTasks.count { it.taskStatus.equals("success", ignoreCase = true) } * 100.0 / totalTasks
        } else {
            0.0
        }
        
        // 转换时间格式
        val startDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochSecond(startTime),
            ZoneId.systemDefault()
        )
        val endDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochSecond(endTime),
            ZoneId.systemDefault()
        )
        
        return CustomPeriodStatistics(
            startTime = startDateTime,
            endTime = endDateTime,
            totalTasks = totalTasks,
            totalRuntime = totalRuntime,
            activeUsers = activeUsers,
            userStats = userStats,
            gpuStats = gpuStats,
            serverStats = serverStats,
            projectStats = projectStats,
            sleepAnalysis = sleepAnalysis,
            successRate = successRate
        )
    }

    override fun getSleepAnalysis(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): SleepAnalysis {
        return timeAnalysisUtils.analyzeSleepPattern(tasks, startTime, endTime)
    }

    /**
     * 根据时间周期获取任务列表（无缓存）
     * @param timePeriod 时间周期
     * @return 任务列表
     */
    fun getTasksByTimePeriod(timePeriod: TimePeriod): List<GpuTaskInfoModel> {
        return gpuTaskQuery.queryTasks(timePeriod)
    }

    /**
     * 根据自定义时间段获取任务列表（无缓存）
     * @param startTime 开始时间（秒）
     * @param endTime 结束时间（秒）
     * @return 任务列表
     */
    fun getTasksByCustomPeriod(startTime: Long, endTime: Long): List<GpuTaskInfoModel> {
        return gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY, // 使用任意周期，实际使用自定义时间
            startTime = startTime,
            endTime = endTime
        )
    }
}
