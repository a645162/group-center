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
 */
object ReportCachePathManager {
    
    // 缓存根目录
    private const val CACHE_ROOT = "./Cache/Report"
    
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
        return Paths.get(CACHE_ROOT).toAbsolutePath()
    }
    
    /**
     * 确保缓存目录存在
     */
    fun ensureCacheDirectory(): Boolean {
        try {
            val cacheDir = getCacheRootPath().toFile()
            if (!cacheDir.exists()) {
                logger.info("创建缓存目录: ${cacheDir.absolutePath}")
                return cacheDir.mkdirs()
            }
            return true
        } catch (e: Exception) {
            logger.error("创建缓存目录失败", e)
            return false
        }
    }
    
    /**
     * 获取24/48/72小时报告缓存文件路径
     */
    fun getHourlyReportPath(hours: Int, startTime: String, endTime: String): Path {
        val fileName = "${hours}hour_report_${startTime}_${endTime}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
    }
    
    /**
     * 获取今日报告缓存文件路径
     */
    fun getTodayReportPath(date: LocalDate = LocalDate.now()): Path {
        val fileName = "today_report_${date.format(dateFormatter)}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
    }
    
    /**
     * 获取昨日报告缓存文件路径
     */
    fun getYesterdayReportPath(date: LocalDate = LocalDate.now().minusDays(1)): Path {
        val fileName = "yesterday_report_${date.format(dateFormatter)}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
    }
    
    /**
     * 获取周报缓存文件路径
     */
    fun getWeeklyReportPath(date: LocalDate = LocalDate.now()): Path {
        val fileName = "weekly_report_${date.format(dateFormatter)}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
    }
    
    /**
     * 获取月报缓存文件路径
     */
    fun getMonthlyReportPath(month: Int = LocalDate.now().monthValue): Path {
        val currentYear = LocalDate.now().year
        val fileName = "monthly_report_${currentYear}-${month.toString().padStart(2, '0')}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
    }
    
    /**
     * 获取年报缓存文件路径
     */
    fun getYearlyReportPath(year: Int = LocalDate.now().year): Path {
        val fileName = "yearly_report_${year}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
    }
    
    /**
     * 获取统计信息缓存文件路径
     */
    fun getStatisticsPath(cacheKey: String): Path {
        val fileName = "${cacheKey}$CACHE_EXTENSION"
        return getCacheRootPath().resolve(fileName)
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
        val cacheDir = getCacheRootPath().toFile()
        if (!cacheDir.exists() || !cacheDir.isDirectory) {
            return 0
        }
        
        val currentTime = System.currentTimeMillis()
        var cleanedCount = 0
        
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(CACHE_EXTENSION)) {
                val lastModified = file.lastModified()
                if (currentTime - lastModified > maxAgeMillis) {
                    try {
                        if (file.delete()) {
                            cleanedCount++
                            logger.debug("清理过期缓存文件: ${file.name}")
                        }
                    } catch (e: Exception) {
                        logger.error("删除缓存文件失败: ${file.name}", e)
                    }
                }
            }
        }
        
        if (cleanedCount > 0) {
            logger.info("清理过期缓存文件完成，共清理 ${cleanedCount} 个文件")
        }
        
        return cleanedCount
    }
    
    /**
     * 获取缓存目录大小（字节）
     */
    fun getCacheDirectorySize(): Long {
        val cacheDir = getCacheRootPath().toFile()
        if (!cacheDir.exists() || !cacheDir.isDirectory) {
            return 0L
        }
        
        return cacheDir.walk()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    /**
     * 获取缓存文件数量
     */
    fun getCacheFileCount(): Int {
        val cacheDir = getCacheRootPath().toFile()
        if (!cacheDir.exists() || !cacheDir.isDirectory) {
            return 0
        }
        
        return cacheDir.listFiles { file -> file.isFile && file.name.endsWith(CACHE_EXTENSION) }?.size ?: 0
    }
}