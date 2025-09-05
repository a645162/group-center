package com.khm.group.center.service

import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

@Service
class StatisticsService {

    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    @Autowired
    lateinit var statisticsAnalyzer: StatisticsAnalyzer

    // 内存缓存，每小时更新一次
    private val statisticsCache = ConcurrentHashMap<String, Any>()
    private var lastCacheUpdate: Long = 0
    private val CACHE_DURATION = 60 * 60 * 1000L // 1小时

    /**
     * 获取用户统计信息
     */
    fun getUserStatistics(timePeriod: TimePeriod): List<UserStatistics> {
        val cacheKey = "user_stats_${timePeriod.name}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as List<UserStatistics>
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeUserStatistics(tasks)

        cacheData(cacheKey, stats)
        return stats
    }

    /**
     * 获取GPU统计信息
     */
    fun getGpuStatistics(timePeriod: TimePeriod): List<GpuStatistics> {
        val cacheKey = "gpu_stats_${timePeriod.name}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as List<GpuStatistics>
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeGpuStatistics(tasks)

        cacheData(cacheKey, stats)
        return stats
    }

    /**
     * 获取服务器统计信息
     */
    fun getServerStatistics(timePeriod: TimePeriod): List<ServerStatistics> {
        val cacheKey = "server_stats_${timePeriod.name}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as List<ServerStatistics>
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeServerStatistics(tasks)

        cacheData(cacheKey, stats)
        return stats
    }

    /**
     * 获取项目统计信息
     */
    fun getProjectStatistics(timePeriod: TimePeriod): List<ProjectStatistics> {
        val cacheKey = "project_stats_${timePeriod.name}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as List<ProjectStatistics>
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeProjectStatistics(tasks)

        cacheData(cacheKey, stats)
        return stats
    }

    /**
     * 获取时间趋势统计
     */
    fun getTimeTrendStatistics(timePeriod: TimePeriod): TimeTrendStatistics {
        val cacheKey = "time_trend_${timePeriod.name}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as TimeTrendStatistics
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeTimeTrendStatistics(tasks, timePeriod)

        cacheData(cacheKey, stats)
        return stats
    }

    /**
     * 获取日报数据
     */
    fun getDailyReport(date: LocalDate = LocalDate.now()): DailyReport {
        val cacheKey = "daily_report_${date}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as DailyReport
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_WEEK)
        val dailyTasks = tasks.filter { task ->
            val taskDate = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(task.taskStartTime),
                ZoneId.systemDefault()
            ).toLocalDate()
            taskDate == date
        }

        val report = statisticsAnalyzer.generateDailyReport(dailyTasks, date)

        cacheData(cacheKey, report)
        return report
    }

    /**
     * 获取周报数据
     */
    fun getWeeklyReport(): WeeklyReport {
        val cacheKey = "weekly_report_${LocalDate.now()}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as WeeklyReport
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_MONTH)
        val report = statisticsAnalyzer.generateWeeklyReport(tasks)

        cacheData(cacheKey, report)
        return report
    }

    /**
     * 获取月报数据
     */
    fun getMonthlyReport(): MonthlyReport {
        val cacheKey = "monthly_report_${LocalDate.now().monthValue}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as MonthlyReport
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.SIX_MONTH)
        val report = statisticsAnalyzer.generateMonthlyReport(tasks)

        cacheData(cacheKey, report)
        return report
    }

    /**
     * 获取年报数据
     */
    fun getYearlyReport(): YearlyReport {
        val cacheKey = "yearly_report_${LocalDate.now().year}"
        val cached = getCachedData(cacheKey)
        if (cached != null) {
            return cached as YearlyReport
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_YEAR)
        val report = statisticsAnalyzer.generateYearlyReport(tasks)

        cacheData(cacheKey, report)
        return report
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        statisticsCache.clear()
        lastCacheUpdate = 0
    }

    /**
     * 强制更新缓存
     */
    fun forceUpdateCache() {
        clearCache()
        // 预加载常用统计数据
        getUserStatistics(TimePeriod.ONE_WEEK)
        getGpuStatistics(TimePeriod.ONE_WEEK)
        getDailyReport()
    }


    private fun getCachedData(key: String): Any? {
        return if (System.currentTimeMillis() - lastCacheUpdate < CACHE_DURATION) {
            statisticsCache[key]
        } else {
            null
        }
    }

    private fun cacheData(key: String, data: Any) {
        statisticsCache[key] = data
        lastCacheUpdate = System.currentTimeMillis()
    }
}
