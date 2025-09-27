package com.khm.group.center.service.cache

import com.alibaba.fastjson2.JSON
import com.khm.group.center.datatype.statistics.Report
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 报告缓存管理器
 * 实现内存+磁盘双缓存策略
 */
@Component
class ReportCacheManager {
    
    // 内存缓存
    private val memoryCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    
    // 缓存过期时间配置（毫秒）
    private val CACHE_EXPIRY_CONFIG = mapOf(
        // 24/48/72小时报告 - 只有内存缓存，1小时过期
        "24hour_report" to TimeUnit.HOURS.toMillis(1),
        "48hour_report" to TimeUnit.HOURS.toMillis(1),
        "72hour_report" to TimeUnit.HOURS.toMillis(1),
        
        // 今日报告 - 只有内存缓存，1小时过期
        "today_report" to TimeUnit.HOURS.toMillis(1),
        
        // 昨日报告 - 内存+磁盘缓存，24小时过期
        "yesterday_report" to TimeUnit.HOURS.toMillis(24),
        
        // 日报 - 历史数据永不过期，但当日报告只有内存缓存
        "daily_report" to Long.MAX_VALUE,
        
        // 周报 - 历史数据永不过期，但当周报告只有内存缓存
        "weekly_report" to Long.MAX_VALUE,
        
        // 月报 - 历史数据永不过期，但当月报告只有内存缓存
        "monthly_report" to Long.MAX_VALUE,
        
        // 年报 - 历史数据永不过期，但当年报告只有内存缓存
        "yearly_report" to Long.MAX_VALUE,
        
        // 统计信息 - 内存+磁盘缓存，1小时过期
        "user_stats" to TimeUnit.HOURS.toMillis(1),
        "gpu_stats" to TimeUnit.HOURS.toMillis(1),
        "server_stats" to TimeUnit.HOURS.toMillis(1),
        "project_stats" to TimeUnit.HOURS.toMillis(1),
        "time_trend" to TimeUnit.HOURS.toMillis(1)
    )
    
    /**
     * 缓存条目
     */
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val expiryTime: Long
    )
    
    init {
        // 确保所有缓存目录存在并检查版本兼容性
        ReportCachePathManager.ensureCacheDirectories()
        logger.info("Report cache manager initialization completed")
    }
    
    /**
     * 从缓存获取数据（优先内存，其次磁盘）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedData(cacheKey: String): T? {
        // 1. 首先尝试从内存缓存获取
        val memoryEntry = memoryCache[cacheKey]
        if (memoryEntry != null && !isExpired(memoryEntry)) {
            logger.debug("✅ Memory cache hit: $cacheKey")
            return memoryEntry.data as T
        }
        
        // 2. 如果内存缓存未命中或已过期，尝试从磁盘获取
        val diskData = loadFromDisk<T>(cacheKey)
        if (diskData != null) {
            logger.debug("✅ Disk cache hit: $cacheKey")
            
            // 将磁盘数据加载到内存缓存
            val expiryTime = getExpiryTime(cacheKey)
            memoryCache[cacheKey] = CacheEntry(diskData as Any, System.currentTimeMillis(), expiryTime)
            
            return diskData
        }
        
        logger.debug("❌ Cache miss: $cacheKey")
        return null
    }
    
    /**
     * 存储数据到缓存（内存+磁盘）
     */
    fun <T> putCachedData(cacheKey: String, data: T) {
        val expiryTime = getExpiryTime(cacheKey)
        val timestamp = System.currentTimeMillis()
        
        // 1. 存储到内存缓存
        memoryCache[cacheKey] = CacheEntry(data as Any, timestamp, expiryTime)
        logger.debug("💾 Data stored in memory cache: $cacheKey")
        
        // 2. 根据缓存类型决定是否存储到磁盘
        if (shouldPersistToDisk(cacheKey)) {
            saveToDisk(cacheKey, data)
            logger.debug("💾 Data stored in disk cache: $cacheKey")
        }
    }
    
    /**
     * 从磁盘加载缓存数据
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> loadFromDisk(cacheKey: String): T? {
        return try {
            val filePath = ReportCachePathManager.getStatisticsPath(cacheKey)
            if (!ReportCachePathManager.cacheFileExists(filePath)) {
                return null
            }
            
            val jsonContent = Files.readString(filePath)
            if (jsonContent.isBlank()) {
                return null
            }
            
            // 检查文件是否过期
            val lastModified = ReportCachePathManager.getCacheFileLastModified(filePath)
            val expiryTime = getExpiryTime(cacheKey)
            if (System.currentTimeMillis() - lastModified > expiryTime) {
                logger.debug("🗑️ Disk cache expired, deleting file: $cacheKey")
                Files.deleteIfExists(filePath)
                return null
            }
            
            // 反序列化JSON数据
            when {
                cacheKey.startsWith("24hour_report") || cacheKey.startsWith("48hour_report") ||
                cacheKey.startsWith("72hour_report") || cacheKey.startsWith("today_report") ||
                cacheKey.startsWith("yesterday_report") || cacheKey.startsWith("weekly_report") ||
                cacheKey.startsWith("monthly_report") || cacheKey.startsWith("yearly_report") -> {
                    JSON.parseObject(jsonContent, Report::class.java) as? T
                }
                else -> {
                    // 其他统计信息使用泛型反序列化
                    JSON.parseObject(jsonContent, Any::class.java) as? T
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load cache data from disk: $cacheKey", e)
            null
        }
    }
    
    /**
     * 保存数据到磁盘
     */
    private fun <T> saveToDisk(cacheKey: String, data: T) {
        try {
            val filePath = ReportCachePathManager.getStatisticsPath(cacheKey)
            val jsonContent = JSON.toJSONString(data)
            
            Files.writeString(
                filePath, 
                jsonContent,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        } catch (e: Exception) {
            logger.error("Failed to save data to disk: $cacheKey", e)
        }
    }
    
    /**
     * 检查缓存是否过期
     */
    private fun isExpired(entry: CacheEntry<*>): Boolean {
        return System.currentTimeMillis() - entry.timestamp > entry.expiryTime
    }
    
    /**
     * 获取缓存的过期时间
     */
    private fun getExpiryTime(cacheKey: String): Long {
        // 根据缓存键的前缀匹配过期时间
        for ((prefix, expiry) in CACHE_EXPIRY_CONFIG) {
            if (cacheKey.startsWith(prefix)) {
                return expiry
            }
        }
        
        // 默认过期时间：1小时
        return TimeUnit.HOURS.toMillis(1)
    }
    
    /**
     * 判断是否应该持久化到磁盘
     */
    private fun shouldPersistToDisk(cacheKey: String): Boolean {
        // 24/48/72小时报告和今日报告只使用内存缓存
        if (cacheKey.startsWith("24hour_report") ||
            cacheKey.startsWith("48hour_report") ||
            cacheKey.startsWith("72hour_report") ||
            cacheKey.startsWith("today_report")) {
            return false
        }
        
        // 检查是否是当月、当年、当日的报告，这些不持久化到磁盘
        if (isCurrentPeriodReport(cacheKey)) {
            return false
        }
        
        return true  // 其他类型的缓存都支持磁盘持久化
    }
    
    /**
     * 检查是否是当前周期的报告（当月、当年、当日）
     */
    private fun isCurrentPeriodReport(cacheKey: String): Boolean {
        val now = java.time.LocalDate.now()
        
        return when {
            cacheKey.startsWith("daily_report") -> {
                // 检查是否是当日报告
                val dateStr = cacheKey.substringAfter("daily_report_")
                try {
                    val reportDate = java.time.LocalDate.parse(dateStr)
                    reportDate == now
                } catch (e: Exception) {
                    false
                }
            }
            cacheKey.startsWith("weekly_report") -> {
                // 检查是否是当周报告
                val parts = cacheKey.substringAfter("weekly_report_").split("_")
                if (parts.size == 2) {
                    try {
                        val year = parts[0].toInt()
                        val week = parts[1].toInt()
                        val currentWeek = now.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                        year == now.year && week == currentWeek
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    false
                }
            }
            cacheKey.startsWith("monthly_report") -> {
                // 检查是否是当月报告
                val parts = cacheKey.substringAfter("monthly_report_").split("_")
                if (parts.size == 2) {
                    try {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt()
                        year == now.year && month == now.monthValue
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    false
                }
            }
            cacheKey.startsWith("yearly_report") -> {
                // 检查是否是当年报告
                val yearStr = cacheKey.substringAfter("yearly_report_")
                try {
                    val year = yearStr.toInt()
                    year == now.year
                } catch (e: Exception) {
                    false
                }
            }
            else -> false
        }
    }
    
    /**
     * 清除指定缓存
     */
    fun clearCache(cacheKey: String) {
        // 从内存缓存中移除
        memoryCache.remove(cacheKey)
        
        // 从磁盘缓存中删除文件
        try {
            val filePath = ReportCachePathManager.getStatisticsPath(cacheKey)
            Files.deleteIfExists(filePath)
            logger.debug("🗑️ Cache cleared: $cacheKey")
        } catch (e: Exception) {
            logger.error("Failed to clear disk cache: $cacheKey", e)
        }
    }
    
    /**
     * 清除所有缓存
     */
    fun clearAllCache() {
        // 清空内存缓存
        memoryCache.clear()
        
        // 删除所有磁盘缓存文件
        try {
            val cacheDirs = listOf(
                ReportCachePathManager.getHourlyReportPath(),
                ReportCachePathManager.getDailyReportPath(),
                ReportCachePathManager.getWeeklyReportPath(),
                ReportCachePathManager.getMonthlyReportPath(),
                ReportCachePathManager.getYearlyReportPath(),
                ReportCachePathManager.getStatisticsPath()
            )
            
            cacheDirs.forEach { cacheDir ->
                if (Files.exists(cacheDir) && Files.isDirectory(cacheDir)) {
                    Files.walk(cacheDir)
                        .filter { path -> path != cacheDir } // 不删除根目录本身
                        .sorted(Comparator.reverseOrder()) // 先删除子文件和子目录
                        .forEach { path ->
                            try {
                                Files.deleteIfExists(path)
                            } catch (e: Exception) {
                                logger.warn("Failed to delete cache file: $path", e)
                            }
                        }
                }
            }
            
            logger.info("🗑️ All cache cleared")
        } catch (e: Exception) {
            logger.error("Failed to clear all disk cache", e)
        }
    }
    
    /**
     * 清理过期缓存
     */
    fun cleanupExpiredCache(): Int {
        var cleanedCount = 0
        
        // 清理内存中的过期缓存
        val iterator = memoryCache.entries.iterator()
        while (iterator.hasNext()) {
            val (key, entry) = iterator.next()
            if (isExpired(entry)) {
                iterator.remove()
                cleanedCount++
                logger.debug("🗑️ Cleanup expired memory cache: $key")
            }
        }
        
        // 清理磁盘中的过期缓存文件
        cleanedCount += ReportCachePathManager.cleanupExpiredCache(TimeUnit.DAYS.toMillis(30))
        
        if (cleanedCount > 0) {
            logger.info("🗑️ Expired cache cleanup completed, total cleaned: ${cleanedCount} entries")
        }
        
        return cleanedCount
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        val memorySize = memoryCache.size
        val diskFileCount = ReportCachePathManager.getCacheFileCount()
        val diskSize = ReportCachePathManager.getCacheDirectorySize()
        
        return CacheStats(memorySize, diskFileCount, diskSize)
    }
    
    /**
     * 缓存统计信息
     */
    data class CacheStats(
        val memoryEntryCount: Int,
        val diskFileCount: Int,
        val diskSizeBytes: Long
    )
}