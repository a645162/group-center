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
        
        // 日报 - 历史数据永不过期
        "daily_report" to Long.MAX_VALUE,
        
        // 周报 - 历史数据永不过期
        "weekly_report" to Long.MAX_VALUE,
        
        // 月报 - 历史数据永不过期
        "monthly_report" to Long.MAX_VALUE,
        
        // 年报 - 历史数据永不过期
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
        // 确保缓存目录存在
        ReportCachePathManager.ensureCacheDirectory()
        logger.info("报告缓存管理器初始化完成")
    }
    
    /**
     * 从缓存获取数据（优先内存，其次磁盘）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedData(cacheKey: String): T? {
        // 1. 首先尝试从内存缓存获取
        val memoryEntry = memoryCache[cacheKey]
        if (memoryEntry != null && !isExpired(memoryEntry)) {
            logger.debug("✅ 内存缓存命中: $cacheKey")
            return memoryEntry.data as T
        }
        
        // 2. 如果内存缓存未命中或已过期，尝试从磁盘获取
        val diskData = loadFromDisk<T>(cacheKey)
        if (diskData != null) {
            logger.debug("✅ 磁盘缓存命中: $cacheKey")
            
            // 将磁盘数据加载到内存缓存
            val expiryTime = getExpiryTime(cacheKey)
            memoryCache[cacheKey] = CacheEntry(diskData as Any, System.currentTimeMillis(), expiryTime)
            
            return diskData
        }
        
        logger.debug("❌ 缓存未命中: $cacheKey")
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
        logger.debug("💾 数据已存储到内存缓存: $cacheKey")
        
        // 2. 根据缓存类型决定是否存储到磁盘
        if (shouldPersistToDisk(cacheKey)) {
            saveToDisk(cacheKey, data)
            logger.debug("💾 数据已存储到磁盘缓存: $cacheKey")
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
                logger.debug("🗑️ 磁盘缓存已过期，删除文件: $cacheKey")
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
            logger.error("从磁盘加载缓存数据失败: $cacheKey", e)
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
            logger.error("保存数据到磁盘失败: $cacheKey", e)
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
        return when {
            cacheKey.startsWith("24hour_report") -> false
            cacheKey.startsWith("48hour_report") -> false
            cacheKey.startsWith("72hour_report") -> false
            cacheKey.startsWith("today_report") -> false
            else -> true  // 其他类型的缓存都支持磁盘持久化
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
            logger.debug("🗑️ 清除缓存: $cacheKey")
        } catch (e: Exception) {
            logger.error("清除磁盘缓存失败: $cacheKey", e)
        }
    }
    
    /**
     * 清除所有缓存
     */
    fun clearAllCache() {
        // 清空内存缓存
        memoryCache.clear()
        
        // 删除磁盘缓存文件
        try {
            val cacheDir = ReportCachePathManager.getCacheRootPath().toFile()
            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".json")) {
                        file.delete()
                    }
                }
            }
            logger.info("🗑️ 所有缓存已清除")
        } catch (e: Exception) {
            logger.error("清除所有磁盘缓存失败", e)
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
                logger.debug("🗑️ 清理过期内存缓存: $key")
            }
        }
        
        // 清理磁盘中的过期缓存文件
        cleanedCount += ReportCachePathManager.cleanupExpiredCache(TimeUnit.DAYS.toMillis(30))
        
        if (cleanedCount > 0) {
            logger.info("🗑️ 过期缓存清理完成，共清理 ${cleanedCount} 个条目")
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