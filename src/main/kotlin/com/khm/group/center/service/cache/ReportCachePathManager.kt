package com.khm.group.center.service.cache

import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 报告缓存路径管理器
 * 负责管理缓存文件的目录结构和文件路径
 * 每种报告类型一个目录，支持版本管理
 */
object ReportCachePathManager {
    
    // 缓存根目录（使用绝对路径）
    private val CACHE_ROOT: String = Paths.get("").toAbsolutePath().resolve("Cache").toString()
    
    // 报告类型子目录
    private const val HOURLY_REPORT_DIR = "HourlyReport"
    private const val DAILY_REPORT_DIR = "DailyReport"
    private const val WEEKLY_REPORT_DIR = "WeeklyReport"
    private const val MONTHLY_REPORT_DIR = "MonthlyReport"
    private const val YEARLY_REPORT_DIR = "YearlyReport"
    private const val STATISTICS_DIR = "Statistics"
    
    // 缓存文件扩展名
    private const val CACHE_EXTENSION = ".json"
    
    // 日期格式化器
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
    
    /**
     * 获取缓存根目录路径
     */
    fun getCacheRootPath(): Path {
        return Paths.get(CACHE_ROOT)
    }
    
    /**
     * 确保所有缓存目录存在并检查版本兼容性
     */
    fun ensureCacheDirectories(): Boolean {
        val directories = listOf(
            getHourlyReportPath(),
            getDailyReportPath(),
            getWeeklyReportPath(),
            getMonthlyReportPath(),
            getYearlyReportPath(),
            getStatisticsPath()
        )
        
        var allSuccess = true
        directories.forEach { dirPath ->
            val needCleanup = CacheVersionManager.ensureCacheDirectoryWithVersion(dirPath)
            if (needCleanup) {
                logger.info("Need to cleanup cache directory: $dirPath")
                CacheVersionManager.cleanupCacheDirectory(dirPath)
            }
            allSuccess = allSuccess && Files.exists(dirPath)
        }
        
        return allSuccess
    }
    
    /**
     * 获取24/48/72小时报告目录路径
     */
    fun getHourlyReportPath(): Path {
        return getCacheRootPath().resolve(HOURLY_REPORT_DIR)
    }
    
    /**
     * 获取日报目录路径
     */
    fun getDailyReportPath(): Path {
        return getCacheRootPath().resolve(DAILY_REPORT_DIR)
    }
    
    /**
     * 获取周报目录路径
     */
    fun getWeeklyReportPath(): Path {
        return getCacheRootPath().resolve(WEEKLY_REPORT_DIR)
    }
    
    /**
     * 获取月报目录路径
     */
    fun getMonthlyReportPath(): Path {
        return getCacheRootPath().resolve(MONTHLY_REPORT_DIR)
    }
    
    /**
     * 获取年报目录路径
     */
    fun getYearlyReportPath(): Path {
        return getCacheRootPath().resolve(YEARLY_REPORT_DIR)
    }
    
    /**
     * 获取统计信息目录路径
     */
    fun getStatisticsPath(): Path {
        return getCacheRootPath().resolve(STATISTICS_DIR)
    }
    
    /**
     * 获取24/48/72小时报告缓存文件路径
     */
    fun getHourlyReportPath(hours: Int, startTime: String, endTime: String): Path {
        val fileName = "${hours}hour_report_${startTime}_${endTime}$CACHE_EXTENSION"
        return getHourlyReportPath().resolve(fileName)
    }
    
    /**
     * 获取今日报告缓存文件路径
     */
    fun getTodayReportPath(date: LocalDate = LocalDate.now()): Path {
        val fileName = "today_report_${date.format(dateFormatter)}$CACHE_EXTENSION"
        return getDailyReportPath().resolve(fileName)
    }
    
    /**
     * 获取昨日报告缓存文件路径
     */
    fun getYesterdayReportPath(date: LocalDate = LocalDate.now().minusDays(1)): Path {
        val fileName = "yesterday_report_${date.format(dateFormatter)}$CACHE_EXTENSION"
        return getDailyReportPath().resolve(fileName)
    }
    
    /**
     * 获取日报缓存文件路径
     */
    fun getDailyReportPath(date: LocalDate): Path {
        val fileName = "daily_report_${date.format(dateFormatter)}$CACHE_EXTENSION"
        return getDailyReportPath().resolve(fileName)
    }
    
    /**
     * 获取日期范围日报缓存文件路径
     */
    fun getDailyReportPath(startDate: LocalDate, endDate: LocalDate): Path {
        val fileName = "daily_report_${startDate.format(dateFormatter)}_${endDate.format(dateFormatter)}$CACHE_EXTENSION"
        return getDailyReportPath().resolve(fileName)
    }
    
    /**
     * 获取周报缓存文件路径
     */
    fun getWeeklyReportPath(year: Int, week: Int): Path {
        val fileName = "weekly_report_${year}_${week}$CACHE_EXTENSION"
        return getWeeklyReportPath().resolve(fileName)
    }
    
    /**
     * 获取月报缓存文件路径
     */
    fun getMonthlyReportPath(year: Int, month: Int): Path {
        val fileName = "monthly_report_${year}_${month}$CACHE_EXTENSION"
        return getMonthlyReportPath().resolve(fileName)
    }
    
    /**
     * 获取年报缓存文件路径
     */
    fun getYearlyReportPath(year: Int): Path {
        val fileName = "yearly_report_${year}$CACHE_EXTENSION"
        return getYearlyReportPath().resolve(fileName)
    }
    
    /**
     * 根据缓存键获取对应的文件路径
     */
    fun getStatisticsPath(cacheKey: String): Path {
        return when {
            cacheKey.startsWith("24hour_report") || cacheKey.startsWith("48hour_report") || 
            cacheKey.startsWith("72hour_report") -> {
                // 解析小时报告的时间信息
                val parts = cacheKey.split("_")
                if (parts.size >= 3) {
                    val hours = parts[0].replace("hour_report", "").toIntOrNull() ?: 24
                    val startTime = parts[1]
                    val endTime = parts[2]
                    getHourlyReportPath(hours, startTime, endTime)
                } else {
                    getHourlyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            cacheKey.startsWith("today_report") -> {
                val dateStr = cacheKey.substringAfter("today_report_")
                try {
                    val date = LocalDate.parse(dateStr)
                    getTodayReportPath(date)
                } catch (e: Exception) {
                    getDailyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            cacheKey.startsWith("yesterday_report") -> {
                val dateStr = cacheKey.substringAfter("yesterday_report_")
                try {
                    val date = LocalDate.parse(dateStr)
                    getYesterdayReportPath(date)
                } catch (e: Exception) {
                    getDailyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            cacheKey.startsWith("daily_report") -> {
                if (cacheKey.contains("_")) {
                    val parts = cacheKey.substringAfter("daily_report_").split("_")
                    if (parts.size == 2) {
                        try {
                            val startDate = LocalDate.parse(parts[0])
                            val endDate = LocalDate.parse(parts[1])
                            getDailyReportPath(startDate, endDate)
                        } catch (e: Exception) {
                            getDailyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                        }
                    } else {
                        try {
                            val date = LocalDate.parse(parts[0])
                            getDailyReportPath(date)
                        } catch (e: Exception) {
                            getDailyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                        }
                    }
                } else {
                    getDailyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            cacheKey.startsWith("weekly_report") -> {
                val parts = cacheKey.substringAfter("weekly_report_").split("_")
                if (parts.size == 2) {
                    try {
                        val year = parts[0].toInt()
                        val week = parts[1].toInt()
                        getWeeklyReportPath(year, week)
                    } catch (e: Exception) {
                        getWeeklyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                    }
                } else {
                    getWeeklyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            cacheKey.startsWith("monthly_report") -> {
                val parts = cacheKey.substringAfter("monthly_report_").split("_")
                if (parts.size == 2) {
                    try {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt()
                        getMonthlyReportPath(year, month)
                    } catch (e: Exception) {
                        getMonthlyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                    }
                } else {
                    getMonthlyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            cacheKey.startsWith("yearly_report") -> {
                val yearStr = cacheKey.substringAfter("yearly_report_")
                try {
                    val year = yearStr.toInt()
                    getYearlyReportPath(year)
                } catch (e: Exception) {
                    getYearlyReportPath().resolve("${cacheKey}$CACHE_EXTENSION")
                }
            }
            else -> {
                // 其他统计信息
                getStatisticsPath().resolve("${cacheKey}$CACHE_EXTENSION")
            }
        }
    }
    
    /**
     * 检查缓存文件是否存在
     */
    fun cacheFileExists(path: Path): Boolean {
        return Files.exists(path)
    }
    
    /**
     * 获取缓存文件最后修改时间
     */
    fun getCacheFileLastModified(path: Path): Long {
        return try {
            Files.getLastModifiedTime(path).toMillis()
        } catch (e: Exception) {
            logger.error("获取缓存文件修改时间失败: ${path}", e)
            -1L
        }
    }
    
    /**
     * 清理过期缓存文件
     */
    fun cleanupExpiredCache(maxAgeMillis: Long): Int {
        val cacheDirs = listOf(
            getHourlyReportPath(),
            getDailyReportPath(),
            getWeeklyReportPath(),
            getMonthlyReportPath(),
            getYearlyReportPath(),
            getStatisticsPath()
        )
        
        var totalCleanedCount = 0
        val currentTime = System.currentTimeMillis()
        
        cacheDirs.forEach { cacheDir ->
            if (!Files.exists(cacheDir) || !Files.isDirectory(cacheDir)) {
                return@forEach
            }
            
            Files.walk(cacheDir)
                .filter { path -> Files.isRegularFile(path) && path.toString().endsWith(CACHE_EXTENSION) }
                .forEach { file ->
                    try {
                        val lastModified = Files.getLastModifiedTime(file).toMillis()
                        if (currentTime - lastModified > maxAgeMillis) {
                            Files.deleteIfExists(file)
                            totalCleanedCount++
                            logger.debug("Cleanup expired cache file: ${file.fileName}")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to delete cache file: ${file.fileName}", e)
                    }
                }
        }
        
        if (totalCleanedCount > 0) {
            logger.info("Expired cache files cleanup completed, total cleaned: ${totalCleanedCount} files")
        }
        
        return totalCleanedCount
    }
    
    /**
     * 获取缓存目录总大小（字节）
     */
    fun getCacheDirectorySize(): Long {
        val cacheDirs = listOf(
            getHourlyReportPath(),
            getDailyReportPath(),
            getWeeklyReportPath(),
            getMonthlyReportPath(),
            getYearlyReportPath(),
            getStatisticsPath()
        )
        
        return cacheDirs.sumOf { dir ->
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                0L
            } else {
                Files.walk(dir)
                    .filter { path -> Files.isRegularFile(path) }
                    .mapToLong { path -> Files.size(path) }
                    .sum()
            }
        }
    }
    
    /**
     * 获取缓存文件总数
     */
    fun getCacheFileCount(): Int {
        val cacheDirs = listOf(
            getHourlyReportPath(),
            getDailyReportPath(),
            getWeeklyReportPath(),
            getMonthlyReportPath(),
            getYearlyReportPath(),
            getStatisticsPath()
        )
        
        return cacheDirs.sumOf { dir ->
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                0
            } else {
                Files.walk(dir)
                    .filter { path -> Files.isRegularFile(path) && path.toString().endsWith(CACHE_EXTENSION) }
                    .count()
                    .toInt()
            }
        }
    }
}