package com.khm.group.center.utils.cache

import com.khm.group.center.datatype.statistics.ReportType
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 报告缓存路径管理器
 * 负责管理报告缓存的文件路径和目录结构
 */
object ReportCachePathManager {
    
    /**
     * 缓存根目录
     */
    val CACHE_ROOT_DIR: String = "./Cache/Report"
    
    /**
     * 获取报告缓存目录
     * @param reportType 报告类型
     * @param date 报告日期（可选）
     * @return 缓存目录路径
     */
    fun getCacheDir(reportType: ReportType, date: LocalDate? = null): String {
        val baseDir = when (reportType) {
            ReportType.TODAY -> "$CACHE_ROOT_DIR/today"
            ReportType.YESTERDAY -> "$CACHE_ROOT_DIR/yesterday"
            ReportType.WEEKLY -> "$CACHE_ROOT_DIR/weekly"
            ReportType.MONTHLY -> "$CACHE_ROOT_DIR/monthly"
            ReportType.YEARLY -> "$CACHE_ROOT_DIR/yearly"
            ReportType.CUSTOM -> "$CACHE_ROOT_DIR/custom"
        }
        
        // 对于需要按日期分目录的报告类型
        return if (date != null && reportType in listOf(ReportType.YESTERDAY, ReportType.MONTHLY, ReportType.YEARLY)) {
            val dateDir = when (reportType) {
                ReportType.YESTERDAY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ReportType.MONTHLY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                ReportType.YEARLY -> date.format(DateTimeFormatter.ofPattern("yyyy"))
                else -> ""
            }
            "$baseDir/$dateDir"
        } else {
            baseDir
        }
    }
    
    /**
     * 获取报告缓存文件路径
     * @param reportType 报告类型
     * @param cacheKey 缓存键
     * @param date 报告日期（可选）
     * @return 缓存文件完整路径
     */
    fun getCacheFilePath(reportType: ReportType, cacheKey: String, date: LocalDate? = null): String {
        val cacheDir = getCacheDir(reportType, date)
        return "$cacheDir/${cacheKey}.json"
    }
    
    /**
     * 确保缓存目录存在
     * @param reportType 报告类型
     * @param date 报告日期（可选）
     */
    fun ensureCacheDirExists(reportType: ReportType, date: LocalDate? = null) {
        val cacheDir = File(getCacheDir(reportType, date))
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * 检查缓存文件是否存在
     * @param reportType 报告类型
     * @param cacheKey 缓存键
     * @param date 报告日期（可选）
     * @return 是否存在
     */
    fun cacheFileExists(reportType: ReportType, cacheKey: String, date: LocalDate? = null): Boolean {
        val cacheFile = File(getCacheFilePath(reportType, cacheKey, date))
        return cacheFile.exists()
    }
    
    /**
     * 获取24/48/72小时报告的缓存键
     * @param hours 小时数
     * @param roundedHour 整点小时（如15表示15:00）
     * @return 缓存键
     */
    fun getHourlyReportCacheKey(hours: Int, roundedHour: Int): String {
        return "${hours}hour_${roundedHour}"
    }
    
    /**
     * 获取今日报告的缓存键（按小时）
     * @param roundedHour 整点小时
     * @return 缓存键
     */
    fun getTodayReportCacheKey(roundedHour: Int): String {
        return "today_${roundedHour}"
    }
    
    /**
     * 获取昨日报告的缓存键
     * @param date 日期
     * @return 缓存键
     */
    fun getYesterdayReportCacheKey(date: LocalDate): String {
        return "yesterday_${date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
    }
    
    /**
     * 获取月报的缓存键
     * @param year 年份
     * @param month 月份
     * @return 缓存键
     */
    fun getMonthlyReportCacheKey(year: Int, month: Int): String {
        return "monthly_${year}-${month.toString().padStart(2, '0')}"
    }
    
    /**
     * 获取年报的缓存键
     * @param year 年份
     * @return 缓存键
     */
    fun getYearlyReportCacheKey(year: Int): String {
        return "yearly_$year"
    }
    
    /**
     * 获取周报的缓存键
     * @param year 年份
     * @param week 周数
     * @return 缓存键
     */
    fun getWeeklyReportCacheKey(year: Int, week: Int): String {
        return "weekly_${year}-W${week.toString().padStart(2, '0')}"
    }
    
    /**
     * 清理过期的缓存文件
     * @param maxAgeDays 最大保留天数
     */
    fun cleanupExpiredCache(maxAgeDays: Int = 30) {
        val cacheRoot = File(CACHE_ROOT_DIR)
        if (!cacheRoot.exists()) return
        
        val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
        
        cacheRoot.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .filter { it.lastModified() < cutoffTime }
            .forEach { it.delete() }
    }
}