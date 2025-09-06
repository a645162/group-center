package com.khm.group.center.service

import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.cache.CacheManager
import com.khm.group.center.utils.time.TimePeriod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class StatisticsService {

    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    @Autowired
    lateinit var statisticsAnalyzer: StatisticsAnalyzer

    @Autowired
    lateinit var cacheManager: CacheManager

    // 缓存过期时间配置
    private val CACHE_DURATION = 60 * 60 * 1000L // 1小时
    private val HOURLY_REPORT_CACHE_DURATION = 60 * 60 * 1000L // 1小时

    /**
     * 获取用户统计信息
     */
    fun getUserStatistics(timePeriod: TimePeriod): List<UserStatistics> {
        val cacheKey = "user_stats_${timePeriod.name}"
        
        // 使用类型安全的缓存获取
        val cached: List<UserStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeUserStatistics(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取GPU统计信息
     */
    fun getGpuStatistics(timePeriod: TimePeriod): List<GpuStatistics> {
        val cacheKey = "gpu_stats_${timePeriod.name}"
        
        // 使用类型安全的缓存获取
        val cached: List<GpuStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeGpuStatistics(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取服务器统计信息
     */
    fun getServerStatistics(timePeriod: TimePeriod): List<ServerStatistics> {
        val cacheKey = "server_stats_${timePeriod.name}"
        
        // 使用类型安全的缓存获取
        val cached: List<ServerStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeServerStatistics(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取项目统计信息
     */
    fun getProjectStatistics(timePeriod: TimePeriod): List<ProjectStatistics> {
        val cacheKey = "project_stats_${timePeriod.name}"
        
        // 使用类型安全的缓存获取
        val cached: List<ProjectStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeProjectStatistics(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取时间趋势统计
     */
    fun getTimeTrendStatistics(timePeriod: TimePeriod): TimeTrendStatistics {
        val cacheKey = "time_trend_${timePeriod.name}"
        
        // 使用类型安全的缓存获取
        val cached: TimeTrendStatistics? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = statisticsAnalyzer.analyzeTimeTrendStatistics(tasks, timePeriod)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取24小时报告数据（从当前时间往前推24小时）
     * 每小时更新一次缓存（在整点刷新）
     */
    fun get24HourReport(): DailyReport {
        val cacheKey = "24hour_report"
        val currentTime = System.currentTimeMillis()
        val currentHour = currentTime / (60 * 60 * 1000) // 当前小时数
        
        // 使用类型安全的缓存获取
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            // 检查是否是当前小时的缓存
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour) {
                return cached
            }
        }

        val currentTimeSeconds = currentTime / 1000
        val startTime = currentTimeSeconds - 24 * 60 * 60 // 24小时前

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = currentTimeSeconds
        )

        val report = statisticsAnalyzer.generate24HourReport(tasks, startTime, currentTimeSeconds)

        // 使用类型安全的缓存存储报告和小时数
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        
        return report
    }

    /**
     * 获取48小时报告数据（从当前时间往前推48小时）
     * 每小时更新一次缓存（在整点过5分钟刷新）
     */
    fun get48HourReport(): DailyReport {
        val cacheKey = "48hour_report"
        val currentTime = System.currentTimeMillis()
        val currentHour = currentTime / (60 * 60 * 1000) // 当前小时数
        val currentMinute = (currentTime % (60 * 60 * 1000)) / (60 * 1000) // 当前分钟数
        
        // 使用类型安全的缓存获取
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            // 检查是否是当前小时的缓存，并且已经过了5分钟刷新时间
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour && currentMinute >= 5) {
                return cached
            }
        }

        val currentTimeSeconds = currentTime / 1000
        val startTime = currentTimeSeconds - 48 * 60 * 60 // 48小时前

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = currentTimeSeconds
        )

        val report = statisticsAnalyzer.generate24HourReport(tasks, startTime, currentTimeSeconds)

        // 使用类型安全的缓存存储报告和小时数
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        
        return report
    }

    /**
     * 获取72小时报告数据（从当前时间往前推72小时）
     * 每小时更新一次缓存（在整点过10分钟刷新）
     */
    fun get72HourReport(): DailyReport {
        val cacheKey = "72hour_report"
        val currentTime = System.currentTimeMillis()
        val currentHour = currentTime / (60 * 60 * 1000) // 当前小时数
        val currentMinute = (currentTime % (60 * 60 * 1000)) / (60 * 1000) // 当前分钟数
        
        // 使用类型安全的缓存获取
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            // 检查是否是当前小时的缓存，并且已经过了10分钟刷新时间
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour && currentMinute >= 10) {
                return cached
            }
        }

        val currentTimeSeconds = currentTime / 1000
        val startTime = currentTimeSeconds - 72 * 60 * 60 // 72小时前

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = currentTimeSeconds
        )

        val report = statisticsAnalyzer.generate24HourReport(tasks, startTime, currentTimeSeconds)

        // 使用类型安全的缓存存储报告和小时数
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        
        return report
    }

    /**
     * 获取日报数据（按自然日统计）
     * 默认获取昨天的日报（昨天凌晨12点到今天凌晨12点）
     */
    fun getDailyReport(date: LocalDate = LocalDate.now().minusDays(1)): DailyReport {
        val cacheKey = "daily_report_${date}"
        
        // 使用类型安全的缓存获取
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
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

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 获取周报数据
     */
    fun getWeeklyReport(): WeeklyReport {
        val cacheKey = "weekly_report_${LocalDate.now()}"
        
        // 使用类型安全的缓存获取
        val cached: WeeklyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_MONTH)
        val report = statisticsAnalyzer.generateWeeklyReport(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 获取月报数据
     */
    fun getMonthlyReport(): MonthlyReport {
        val cacheKey = "monthly_report_${LocalDate.now().monthValue}"
        
        // 使用类型安全的缓存获取
        val cached: MonthlyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.SIX_MONTH)
        val report = statisticsAnalyzer.generateMonthlyReport(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 获取年报数据
     */
    fun getYearlyReport(): YearlyReport {
        val cacheKey = "yearly_report_${LocalDate.now().year}"
        
        // 使用类型安全的缓存获取
        val cached: YearlyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            return cached
        }

        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_YEAR)
        val report = statisticsAnalyzer.generateYearlyReport(tasks)

        // 使用类型安全的缓存存储
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        cacheManager.clearAllCache()
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
        get24HourReport() // 预加载24小时报告
        get48HourReport() // 预加载48小时报告
        get72HourReport() // 预加载72小时报告
    }
}
