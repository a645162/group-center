package com.khm.group.center.service

import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.cache.CacheManager
import com.khm.group.center.utils.time.TimePeriod
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * 缓存统计服务 - 包装基础服务并添加缓存逻辑
 * 现有统计API使用此服务
 */
@Service
class CachedStatisticsService {

    @Autowired
    private lateinit var baseStatisticsService: BaseStatisticsService

    @Autowired
    private lateinit var gpuTaskQuery: GpuTaskQuery

    @Autowired
    private lateinit var cacheManager: CacheManager

    private val logger = LoggerFactory.getLogger(CachedStatisticsService::class.java)

    // 缓存过期时间配置
    private val CACHE_DURATION = 60 * 60 * 1000L // 1小时
    private val HOURLY_REPORT_CACHE_DURATION = 60 * 60 * 1000L // 1小时

    /**
     * 获取用户统计信息（带缓存）
     */
    fun getUserStatistics(timePeriod: TimePeriod): List<UserStatistics> {
        val cacheKey = "user_stats_${timePeriod.name}"
        
        // 尝试从缓存获取
        val cached: List<UserStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取用户统计：$timePeriod")
            return cached
        }

        logger.info("缓存未命中，重新计算用户统计：$timePeriod")
        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = baseStatisticsService.getUserStatistics(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取GPU统计信息（带缓存）
     */
    fun getGpuStatistics(timePeriod: TimePeriod): List<GpuStatistics> {
        val cacheKey = "gpu_stats_${timePeriod.name}"
        
        // 尝试从缓存获取
        val cached: List<GpuStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取GPU统计：$timePeriod")
            return cached
        }

        logger.info("缓存未命中，重新计算GPU统计：$timePeriod")
        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = baseStatisticsService.getGpuStatistics(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取服务器统计信息（带缓存）
     */
    fun getServerStatistics(timePeriod: TimePeriod): List<ServerStatistics> {
        val cacheKey = "server_stats_${timePeriod.name}"
        
        // 尝试从缓存获取
        val cached: List<ServerStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取服务器统计：$timePeriod")
            return cached
        }

        logger.info("缓存未命中，重新计算服务器统计：$timePeriod")
        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = baseStatisticsService.getServerStatistics(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取项目统计信息（带缓存）
     */
    fun getProjectStatistics(timePeriod: TimePeriod): List<ProjectStatistics> {
        val cacheKey = "project_stats_${timePeriod.name}"
        
        // 尝试从缓存获取
        val cached: List<ProjectStatistics>? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取项目统计：$timePeriod")
            return cached
        }

        logger.info("缓存未命中，重新计算项目统计：$timePeriod")
        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = baseStatisticsService.getProjectStatistics(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取时间趋势统计信息（带缓存）
     */
    fun getTimeTrendStatistics(timePeriod: TimePeriod): TimeTrendStatistics {
        val cacheKey = "time_trend_${timePeriod.name}"
        
        // 尝试从缓存获取
        val cached: TimeTrendStatistics? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取时间趋势统计：$timePeriod")
            return cached
        }

        logger.info("缓存未命中，重新计算时间趋势统计：$timePeriod")
        val tasks = gpuTaskQuery.queryTasks(timePeriod)
        val stats = baseStatisticsService.getTimeTrendStatistics(tasks, timePeriod)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, stats, CACHE_DURATION)
        return stats
    }

    /**
     * 获取24小时报告数据（带缓存）
     */
    fun get24HourReport(): DailyReport {
        val cacheKey = "24hour_report"
        val currentTime = System.currentTimeMillis()
        val currentHour = currentTime / (60 * 60 * 1000) // 当前小时数
        
        // 检查缓存有效性
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour) {
                logger.debug("从缓存获取24小时报告")
                return cached
            }
        }

        logger.info("缓存未命中，重新计算24小时报告")
        val currentTimeSeconds = currentTime / 1000
        val startTime = currentTimeSeconds - 24 * 60 * 60 // 24小时前

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = currentTimeSeconds
        )

        val report = baseStatisticsService.generate24HourReport(tasks, startTime, currentTimeSeconds)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        
        return report
    }

    /**
     * 获取48小时报告数据（带缓存）
     */
    fun get48HourReport(): DailyReport {
        val cacheKey = "48hour_report"
        val currentTime = System.currentTimeMillis()
        val currentHour = currentTime / (60 * 60 * 1000)
        val currentMinute = (currentTime % (60 * 60 * 1000)) / (60 * 1000)
        
        // 检查缓存有效性
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour && currentMinute >= 5) {
                logger.debug("从缓存获取48小时报告")
                return cached
            }
        }

        logger.info("缓存未命中，重新计算48小时报告")
        val currentTimeSeconds = currentTime / 1000
        val startTime = currentTimeSeconds - 48 * 60 * 60

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = currentTimeSeconds
        )

        val report = baseStatisticsService.generate24HourReport(tasks, startTime, currentTimeSeconds)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        
        return report
    }

    /**
     * 获取72小时报告数据（带缓存）
     */
    fun get72HourReport(): DailyReport {
        val cacheKey = "72hour_report"
        val currentTime = System.currentTimeMillis()
        val currentHour = currentTime / (60 * 60 * 1000)
        val currentMinute = (currentTime % (60 * 60 * 1000)) / (60 * 1000)
        
        // 检查缓存有效性
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour && currentMinute >= 10) {
                logger.debug("从缓存获取72小时报告")
                return cached
            }
        }

        logger.info("缓存未命中，重新计算72小时报告")
        val currentTimeSeconds = currentTime / 1000
        val startTime = currentTimeSeconds - 72 * 60 * 60

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = currentTimeSeconds
        )

        val report = baseStatisticsService.generate24HourReport(tasks, startTime, currentTimeSeconds)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        
        return report
    }

    /**
     * 获取日报数据（带缓存）
     */
    fun getDailyReport(date: LocalDate = LocalDate.now().minusDays(1)): DailyReport {
        val cacheKey = "daily_report_${date}"
        
        // 尝试从缓存获取
        val cached: DailyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取日报：$date")
            return cached
        }

        logger.info("缓存未命中，重新计算日报：$date")
        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_WEEK)
        val report = baseStatisticsService.generateDailyReport(tasks, date)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 获取周报数据（带缓存）
     */
    fun getWeeklyReport(): WeeklyReport {
        val cacheKey = "weekly_report_${LocalDate.now()}"
        
        // 尝试从缓存获取
        val cached: WeeklyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取周报")
            return cached
        }

        logger.info("缓存未命中，重新计算周报")
        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_MONTH)
        val report = baseStatisticsService.generateWeeklyReport(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 获取月报数据（带缓存）
     */
    fun getMonthlyReport(): MonthlyReport {
        val cacheKey = "monthly_report_${LocalDate.now().monthValue}"
        
        // 尝试从缓存获取
        val cached: MonthlyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取月报")
            return cached
        }

        logger.info("缓存未命中，重新计算月报")
        val tasks = gpuTaskQuery.queryTasks(TimePeriod.SIX_MONTH)
        val report = baseStatisticsService.generateMonthlyReport(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 获取年报数据（带缓存）
     */
    fun getYearlyReport(): YearlyReport {
        val cacheKey = "yearly_report_${LocalDate.now().year}"
        
        // 尝试从缓存获取
        val cached: YearlyReport? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.debug("从缓存获取年报")
            return cached
        }

        logger.info("缓存未命中，重新计算年报")
        val tasks = gpuTaskQuery.queryTasks(TimePeriod.ONE_YEAR)
        val report = baseStatisticsService.generateYearlyReport(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        return report
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        logger.info("清除统计缓存")
        cacheManager.clearAllCache()
    }

    /**
     * 强制更新缓存
     */
    fun forceUpdateCache() {
        logger.info("强制更新统计缓存")
        clearCache()
        // 预加载常用统计数据
        getUserStatistics(TimePeriod.ONE_WEEK)
        getGpuStatistics(TimePeriod.ONE_WEEK)
        getDailyReport()
        get24HourReport()
        get48HourReport()
        get72HourReport()
    }
}