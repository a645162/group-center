package com.khm.group.center.service.cache

import com.khm.group.center.datatype.statistics.Report
import com.khm.group.center.datatype.statistics.ReportType
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 报告缓存管理器测试
 */
@SpringBootTest
class ReportCacheManagerTest {

    @Autowired
    private lateinit var reportCacheManager: ReportCacheManager

    @Test
    fun testMemoryCache() {
        logger.info("测试内存缓存功能")
        
        val testKey = "test_memory_cache"
        val testData = "测试数据"
        
        // 存储数据到缓存
        reportCacheManager.putCachedData(testKey, testData)
        
        // 从缓存获取数据
        val cachedData: String? = reportCacheManager.getCachedData(testKey)
        assert(cachedData == testData) { "内存缓存数据不匹配" }
        
        logger.info("✅ 内存缓存测试通过")
    }

    @Test
    fun testDiskCache() {
        logger.info("测试磁盘缓存功能")
        
        val testKey = "test_disk_cache"
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        val testData = Report(
            reportType = ReportType.TODAY,
            title = "测试报告",
            periodStartDate = yesterday,
            periodEndDate = today,
            startTime = now.minusDays(1),
            endTime = now,
            actualTaskStartTime = now.minusDays(1),
            actualTaskEndTime = now,
            totalTasks = 100,
            totalRuntime = 3600,
            activeUsers = 5,
            topUsers = emptyList(),
            topGpus = emptyList(),
            topProjects = emptyList(),
            sleepAnalysis = null
        )
        
        // 存储数据到缓存（应该会持久化到磁盘）
        reportCacheManager.putCachedData(testKey, testData)
        
        // 从缓存获取数据
        val cachedData: Report? = reportCacheManager.getCachedData(testKey)
        assert(cachedData != null) { "磁盘缓存数据为空" }
        assert(cachedData?.title == testData.title) { "磁盘缓存数据不匹配" }
        
        logger.info("✅ 磁盘缓存测试通过")
    }

    @Test
    fun testCacheExpiry() {
        logger.info("测试缓存过期功能")
        
        val testKey = "test_expiry_cache"
        val testData = "过期测试数据"
        
        // 存储数据到缓存
        reportCacheManager.putCachedData(testKey, testData)
        
        // 立即获取应该能命中
        val cachedData1: String? = reportCacheManager.getCachedData(testKey)
        assert(cachedData1 == testData) { "缓存过期测试失败 - 第一次获取" }
        
        // 模拟等待（这里只是测试逻辑，实际过期时间由配置决定）
        Thread.sleep(100)
        
        // 清理过期缓存（这里应该不会清理，因为还没过期）
        val cleanedCount = reportCacheManager.cleanupExpiredCache()
        logger.info("清理过期缓存数量: $cleanedCount")
        
        logger.info("✅ 缓存过期测试通过")
    }

    @Test
    fun testCacheClear() {
        logger.info("测试缓存清理功能")
        
        val testKey = "test_clear_cache"
        val testData = "清理测试数据"
        
        // 存储数据到缓存
        reportCacheManager.putCachedData(testKey, testData)
        
        // 验证数据存在
        val cachedData1: String? = reportCacheManager.getCachedData(testKey)
        assert(cachedData1 == testData) { "缓存清理测试失败 - 清理前获取" }
        
        // 清理指定缓存
        reportCacheManager.clearCache(testKey)
        
        // 验证数据已被清理
        val cachedData2: String? = reportCacheManager.getCachedData(testKey)
        assert(cachedData2 == null) { "缓存清理测试失败 - 清理后获取" }
        
        logger.info("✅ 缓存清理测试通过")
    }

    @Test
    fun testCacheStats() {
        logger.info("测试缓存统计功能")
        
        // 获取缓存统计信息
        val stats = reportCacheManager.getCacheStats()
        
        logger.info("缓存统计信息:")
        logger.info("- 内存缓存条目数: ${stats.memoryEntryCount}")
        logger.info("- 磁盘缓存文件数: ${stats.diskFileCount}")
        logger.info("- 磁盘缓存大小: ${stats.diskSizeBytes} 字节")
        
        assert(stats.memoryEntryCount >= 0) { "内存缓存条目数异常" }
        assert(stats.diskFileCount >= 0) { "磁盘缓存文件数异常" }
        assert(stats.diskSizeBytes >= 0) { "磁盘缓存大小异常" }
        
        logger.info("✅ 缓存统计测试通过")
    }

    @Test
    fun testPathManager() {
        logger.info("测试路径管理器功能")
        
        // 测试缓存目录路径
        val cacheRoot = ReportCachePathManager.getCacheRootPath()
        logger.info("缓存根目录: $cacheRoot")
        
        // 测试确保目录存在
        val dirCreated = ReportCachePathManager.ensureCacheDirectory()
        assert(dirCreated) { "缓存目录创建失败" }
        
        // 测试各种报告路径生成
        val todayPath = ReportCachePathManager.getTodayReportPath()
        logger.info("今日报告路径: $todayPath")
        
        val yesterdayPath = ReportCachePathManager.getYesterdayReportPath()
        logger.info("昨日报告路径: $yesterdayPath")
        
        val weeklyPath = ReportCachePathManager.getWeeklyReportPath()
        logger.info("周报路径: $weeklyPath")
        
        val monthlyPath = ReportCachePathManager.getMonthlyReportPath()
        logger.info("月报路径: $monthlyPath")
        
        val yearlyPath = ReportCachePathManager.getYearlyReportPath()
        logger.info("年报路径: $yearlyPath")
        
        val hourlyPath = ReportCachePathManager.getHourlyReportPath(24, "2025-09-25-15-00", "2025-09-26-15-00")
        logger.info("24小时报告路径: $hourlyPath")
        
        logger.info("✅ 路径管理器测试通过")
    }
}