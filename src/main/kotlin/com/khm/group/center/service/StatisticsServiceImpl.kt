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

    override fun generate24HourReport(tasks: List<GpuTaskInfoModel>, startTimestamp: Long, endTimestamp: Long): Report {
        return statisticsAnalyzer.generate24HourReport(tasks, startTimestamp, endTimestamp)
    }

    override fun generateDailyReport(tasks: List<GpuTaskInfoModel>, date: LocalDate?): Report {
        val targetDate = date ?: LocalDate.now().minusDays(1)
        return statisticsAnalyzer.generateDailyReport(tasks, targetDate)
    }

    override fun generateDailyReport(tasks: List<GpuTaskInfoModel>, startDate: LocalDate, endDate: LocalDate): Report {
        return statisticsAnalyzer.generateDailyReport(tasks, startDate, endDate)
    }

    override fun generateWeeklyReport(tasks: List<GpuTaskInfoModel>, year: Int?, week: Int?): Report {
        return if (year != null || week != null) {
            // 如果指定了年份或周数，使用指定周的报告
            val targetYear = year ?: LocalDate.now().year
            val targetWeek = week ?: LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfYear())
            statisticsAnalyzer.generateWeeklyReport(tasks, targetYear, targetWeek)
        } else {
            // 如果没有指定年份和周数，默认生成上周的报告
            statisticsAnalyzer.generateWeeklyReport(tasks)
        }
    }

    override fun generateMonthlyReport(tasks: List<GpuTaskInfoModel>, year: Int?, month: Int?): Report {
        return if (year != null || month != null) {
            // 如果指定了年份或月份，使用指定月份的报告
            val targetYear = year ?: LocalDate.now().year
            val targetMonth = month ?: LocalDate.now().monthValue
            statisticsAnalyzer.generateMonthlyReport(tasks, targetYear, targetMonth)
        } else {
            // 如果没有指定年份和月份，默认生成上个月的报告
            statisticsAnalyzer.generateMonthlyReport(tasks)
        }
    }

    override fun generateYearlyReport(tasks: List<GpuTaskInfoModel>, year: Int?): Report {
        return if (year != null) {
            // 如果指定了年份，使用指定年份的报告
            val targetYear = year
            statisticsAnalyzer.generateYearlyReport(tasks, targetYear)
        } else {
            // 如果没有指定年份，默认生成去年的报告
            statisticsAnalyzer.generateYearlyReport(tasks)
        }
    }

    override fun getCustomPeriodStatistics(tasks: List<GpuTaskInfoModel>, startTime: Long, endTime: Long): Report {
        return statisticsAnalyzer.generateCustomPeriodReport(tasks, startTime, endTime)
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
