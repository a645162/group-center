package com.khm.group.center.service

import com.khm.group.center.datatype.statistics.*
import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.db.query.GpuTaskQuery
import com.khm.group.center.utils.cache.CacheManager
import com.khm.group.center.utils.time.HourlyTimeUtils
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
     * 获取24小时报告数据（带缓存，向后取整整小时）
     * 例如：现在是14:10，统计昨天15:00到今天15:00
     */
    fun get24HourReport(): Report {
        val cacheKey = "24hour_report"
        val currentHour = HourlyTimeUtils.getCurrentHour()
        
        // 检查缓存有效性
        val cached: Report? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour) {
                logger.info("✅ 缓存命中，从缓存获取24小时报告（当前小时：${currentHour}）")
                return cached
            }
        }

        logger.info("🔄 缓存未命中，重新计算24小时报告（当前小时：${currentHour}）")
        
        // 清除所有旧的24小时报告缓存
        val clearedCount = cacheManager.clearCacheByType("24hour_report")
        if (clearedCount > 0) {
            logger.info("🗑️ 清除旧的24小时报告缓存：${clearedCount}个")
        }
        
        // 使用向后取整的整小时时间范围
        val (startTime, endTime) = HourlyTimeUtils.getRoundedHourRange(24)

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = endTime
        )

        val report = baseStatisticsService.generate24HourReport(tasks, startTime, endTime)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        logger.info("💾 新24小时报告已缓存（当前小时：${currentHour}，时间范围：${startTime} - ${endTime}）")
        
        return report
    }

    /**
     * 获取48小时报告数据（带缓存，向后取整整小时）
     * 例如：现在是14:10，统计前天15:00到今天15:00
     */
    fun get48HourReport(): Report {
        val cacheKey = "48hour_report"
        val currentHour = HourlyTimeUtils.getCurrentHour()
        
        // 检查缓存有效性
        val cached: Report? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour) {
                logger.info("✅ 缓存命中，从缓存获取48小时报告（当前小时：${currentHour}）")
                return cached
            }
        }

        logger.info("🔄 缓存未命中，重新计算48小时报告（当前小时：${currentHour}）")
        
        // 清除所有旧的48小时报告缓存
        val clearedCount = cacheManager.clearCacheByType("48hour_report")
        if (clearedCount > 0) {
            logger.info("🗑️ 清除旧的48小时报告缓存：${clearedCount}个")
        }
        
        // 使用向后取整的整小时时间范围
        val (startTime, endTime) = HourlyTimeUtils.getRoundedHourRange(48)

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = endTime
        )

        val report = baseStatisticsService.generate24HourReport(tasks, startTime, endTime)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        logger.info("💾 新48小时报告已缓存（当前小时：${currentHour}，时间范围：${startTime} - ${endTime}）")
        
        return report
    }

    /**
     * 获取72小时报告数据（带缓存，向后取整整小时）
     * 例如：现在是14:10，统计大前天15:00到今天15:00
     */
    fun get72HourReport(): Report {
        val cacheKey = "72hour_report"
        val currentHour = HourlyTimeUtils.getCurrentHour()
        
        // 检查缓存有效性
        val cached: Report? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            val cachedHour = cacheManager.getCachedData<Long>("${cacheKey}_hour")
            if (cachedHour != null && cachedHour == currentHour) {
                logger.info("✅ 缓存命中，从缓存获取72小时报告（当前小时：${currentHour}）")
                return cached
            }
        }

        logger.info("🔄 缓存未命中，重新计算72小时报告（当前小时：${currentHour}）")
        
        // 清除所有旧的72小时报告缓存
        val clearedCount = cacheManager.clearCacheByType("72hour_report")
        if (clearedCount > 0) {
            logger.info("🗑️ 清除旧的72小时报告缓存：${clearedCount}个")
        }
        
        // 使用向后取整的整小时时间范围
        val (startTime, endTime) = HourlyTimeUtils.getRoundedHourRange(72)

        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_DAY,
            startTime = startTime,
            endTime = endTime
        )

        val report = baseStatisticsService.generate24HourReport(tasks, startTime, endTime)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, HOURLY_REPORT_CACHE_DURATION)
        cacheManager.putCachedData("${cacheKey}_hour", currentHour, HOURLY_REPORT_CACHE_DURATION)
        logger.info("💾 新72小时报告已缓存（当前小时：${currentHour}，时间范围：${startTime} - ${endTime}）")
        
        return report
    }

    /**
     * 获取今日日报数据（带缓存，整点时间范围：今天0:00到明天0:00）
     */
     fun getTodayReport(): Report {
         val date = LocalDate.now()
         val cacheKey = "today_report_${date}"
          
         // 尝试从缓存获取
         val cached: Report? = cacheManager.getCachedData(cacheKey)
         if (cached != null) {
             logger.info("✅ 缓存命中，从缓存获取今日日报（${date}）")
             return cached
         }
  
         logger.info("🔄 缓存未命中，重新计算今日日报（${date}）")
          
         // 清除所有旧的今日日报缓存
         val clearedCount = cacheManager.clearCacheByType("today_report")
         if (clearedCount > 0) {
             logger.info("🗑️ 清除旧的今日日报缓存：${clearedCount}个")
         }
          
         // 使用整点时间范围
         val (startTime, endTime) = HourlyTimeUtils.getTodayRoundedRange()
          
         val tasks = gpuTaskQuery.queryTasks(
             timePeriod = TimePeriod.ONE_DAY,
             startTime = startTime,
             endTime = endTime
         )
         val report = baseStatisticsService.generate24HourReport(tasks, startTime, endTime)
  
         // 存储到缓存
         cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
         logger.info("💾 新今日日报已缓存（${date}，时间范围：${startTime} - ${endTime}）")
         return report
     }
 
     /**
      * 获取昨日日报数据（带缓存，整点时间范围：昨天0:00到今天0:00）
      */
     fun getYesterdayReport(): Report {
         val date = LocalDate.now().minusDays(1)
         val cacheKey = "yesterday_report_${date}"
          
         // 尝试从缓存获取
         val cached: Report? = cacheManager.getCachedData(cacheKey)
         if (cached != null) {
             logger.info("✅ 缓存命中，从缓存获取昨日日报（${date}）")
             return cached
         }
  
         logger.info("🔄 缓存未命中，重新计算昨日日报（${date}）")
          
         // 清除所有旧的昨日日报缓存
         val clearedCount = cacheManager.clearCacheByType("yesterday_report")
         if (clearedCount > 0) {
             logger.info("🗑️ 清除旧的昨日日报缓存：${clearedCount}个")
         }
          
         // 使用整点时间范围
         val (startTime, endTime) = HourlyTimeUtils.getYesterdayRoundedRange()
          
         val tasks = gpuTaskQuery.queryTasks(
             timePeriod = TimePeriod.ONE_DAY,
             startTime = startTime,
             endTime = endTime
         )
         val report = baseStatisticsService.generate24HourReport(tasks, startTime, endTime)
  
         // 存储到缓存
         cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
         logger.info("💾 新昨日日报已缓存（${date}，时间范围：${startTime} - ${endTime}）")
         return report
     }
    /**
     * 获取周报数据（带缓存，整点时间范围：上周一0:00到本周一0:00）
     */
    fun getWeeklyReport(): Report {
        val currentDate = LocalDate.now()
        val cacheKey = "weekly_report_${currentDate}"
        
        // 尝试从缓存获取
        val cached: Report? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.info("✅ 缓存命中，从缓存获取周报（${currentDate}）")
            return cached
        }

        logger.info("🔄 缓存未命中，重新计算周报（${currentDate}）")
        
        // 清除所有旧的周报缓存
        val clearedCount = cacheManager.clearCacheByType("weekly_report")
        if (clearedCount > 0) {
            logger.info("🗑️ 清除旧的周报缓存：${clearedCount}个")
        }
        
        // 使用整点时间范围
        val (startTime, endTime) = HourlyTimeUtils.getWeeklyRoundedRange()
        
        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_WEEK,
            startTime = startTime,
            endTime = endTime
        )
        val report = baseStatisticsService.generateWeeklyReport(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        logger.info("💾 新周报已缓存（${currentDate}，时间范围：${startTime} - ${endTime}）")
        return report
    }

    /**
     * 获取月报数据（带缓存，整点时间范围：上月1号0:00到本月1号0:00）
     */
    fun getMonthlyReport(): Report {
        val currentMonth = LocalDate.now().monthValue
        val cacheKey = "monthly_report_${currentMonth}"
        
        // 尝试从缓存获取
        val cached: Report? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.info("✅ 缓存命中，从缓存获取月报（${currentMonth}月）")
            return cached
        }

        logger.info("🔄 缓存未命中，重新计算月报（${currentMonth}月）")
        
        // 清除所有旧的月报缓存
        val clearedCount = cacheManager.clearCacheByType("monthly_report")
        if (clearedCount > 0) {
            logger.info("🗑️ 清除旧的月报缓存：${clearedCount}个")
        }
        
        // 使用整点时间范围
        val (startTime, endTime) = HourlyTimeUtils.getMonthlyRoundedRange()
        
        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_MONTH,
            startTime = startTime,
            endTime = endTime
        )
        val report = baseStatisticsService.generateMonthlyReport(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        logger.info("💾 新月报已缓存（${currentMonth}月，时间范围：${startTime} - ${endTime}）")
        return report
    }

    /**
     * 获取年报数据（带缓存，整点时间范围：去年1月1号0:00到今年1月1号0:00）
     */
    fun getYearlyReport(): Report {
        val currentYear = LocalDate.now().year
        val cacheKey = "yearly_report_${currentYear}"
        
        // 尝试从缓存获取
        val cached: Report? = cacheManager.getCachedData(cacheKey)
        if (cached != null) {
            logger.info("✅ 缓存命中，从缓存获取年报（${currentYear}年）")
            return cached
        }

        logger.info("🔄 缓存未命中，重新计算年报（${currentYear}年）")
        
        // 清除所有旧的年报缓存
        val clearedCount = cacheManager.clearCacheByType("yearly_report")
        if (clearedCount > 0) {
            logger.info("🗑️ 清除旧的年报缓存：${clearedCount}个")
        }
        
        // 使用整点时间范围
        val (startTime, endTime) = HourlyTimeUtils.getYearlyRoundedRange()
        
        val tasks = gpuTaskQuery.queryTasks(
            timePeriod = TimePeriod.ONE_YEAR,
            startTime = startTime,
            endTime = endTime
        )
        val report = baseStatisticsService.generateYearlyReport(tasks)

        // 存储到缓存
        cacheManager.putCachedData(cacheKey, report, CACHE_DURATION)
        logger.info("💾 新年报已缓存（${currentYear}年，时间范围：${startTime} - ${endTime}）")
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
        getTodayReport()
        getYesterdayReport()
        get24HourReport()
        get48HourReport()
        get72HourReport()
    }
}
