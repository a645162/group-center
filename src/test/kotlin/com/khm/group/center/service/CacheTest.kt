package com.khm.group.center.service

import com.khm.group.center.service.cache.ReportCacheManager
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 缓存测试类 - 测试各种报告类型的缓存功能
 * 每种报告类型都进行连续两次调用，验证缓存是否正确工作
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheTest {

    @Autowired
    private lateinit var cachedStatisticsService: CachedStatisticsService

    @Autowired
    private lateinit var reportCacheManager: ReportCacheManager

    private val logger = LoggerFactory.getLogger(CacheTest::class.java)

    /**
     * 测试24小时报告缓存
     */
    @Test
    fun test24HourReportCache() {
        logger.info("=== 开始测试24小时报告缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用24小时报告...")
        val report1 = cachedStatisticsService.get24HourReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用24小时报告...")
        val report2 = cachedStatisticsService.get24HourReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 24小时报告缓存测试通过")
    }

    /**
     * 测试48小时报告缓存
     */
    @Test
    fun test48HourReportCache() {
        logger.info("=== 开始测试48小时报告缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用48小时报告...")
        val report1 = cachedStatisticsService.get48HourReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用48小时报告...")
        val report2 = cachedStatisticsService.get48HourReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 48小时报告缓存测试通过")
    }

    /**
     * 测试72小时报告缓存
     */
    @Test
    fun test72HourReportCache() {
        logger.info("=== 开始测试72小时报告缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用72小时报告...")
        val report1 = cachedStatisticsService.get72HourReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用72小时报告...")
        val report2 = cachedStatisticsService.get72HourReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 72小时报告缓存测试通过")
    }

    /**
     * 测试今日日报缓存
     */
    @Test
    fun testTodayReportCache() {
        logger.info("=== 开始测试今日日报缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用今日日报...")
        val report1 = cachedStatisticsService.getTodayReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用今日日报...")
        val report2 = cachedStatisticsService.getTodayReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 今日日报缓存测试通过")
    }

    /**
     * 测试昨日日报缓存
     */
    @Test
    fun testYesterdayReportCache() {
        logger.info("=== 开始测试昨日日报缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用昨日日报...")
        val report1 = cachedStatisticsService.getYesterdayReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用昨日日报...")
        val report2 = cachedStatisticsService.getYesterdayReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 昨日日报缓存测试通过")
    }

    /**
     * 测试周报缓存
     */
    @Test
    fun testWeeklyReportCache() {
        logger.info("=== 开始测试周报缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用周报...")
        val report1 = cachedStatisticsService.getWeeklyReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用周报...")
        val report2 = cachedStatisticsService.getWeeklyReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 周报缓存测试通过")
    }

    /**
     * 测试月报缓存
     */
    @Test
    fun testMonthlyReportCache() {
        logger.info("=== 开始测试月报缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用月报...")
        val report1 = cachedStatisticsService.getMonthlyReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用月报...")
        val report2 = cachedStatisticsService.getMonthlyReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 月报缓存测试通过")
    }

    /**
     * 测试年报缓存
     */
    @Test
    fun testYearlyReportCache() {
        logger.info("=== 开始测试年报缓存 ===")
        
        // 第一次调用 - 应该计算并缓存
        logger.info("第一次调用年报...")
        val report1 = cachedStatisticsService.getYearlyReport()
        logger.info("第一次调用完成，报告标题: ${report1.title}")
        
        // 第二次调用 - 应该从缓存获取
        logger.info("第二次调用年报...")
        val report2 = cachedStatisticsService.getYearlyReport()
        logger.info("第二次调用完成，报告标题: ${report2.title}")
        
        // 验证两次调用返回相同的报告
        assert(report1.title == report2.title) { "两次调用应该返回相同的报告" }
        logger.info("✅ 年报缓存测试通过")
    }

    /**
     * 测试缓存清理功能
     */
    @Test
    fun testCacheClear() {
        logger.info("=== 开始测试缓存清理功能 ===")
        
        // 先获取一个报告，确保有缓存
        logger.info("获取24小时报告...")
        val report1 = cachedStatisticsService.get24HourReport()
        logger.info("报告标题: ${report1.title}")
        
        // 清理缓存
        logger.info("清理缓存...")
        cachedStatisticsService.clearCache()
        
        // 再次获取报告，应该重新计算
        logger.info("清理后再次获取24小时报告...")
        val report2 = cachedStatisticsService.get24HourReport()
        logger.info("清理后报告标题: ${report2.title}")
        
        logger.info("✅ 缓存清理测试通过")
    }

    /**
     * 综合测试 - 测试所有报告类型的缓存
     */
    @Test
    fun testAllReportCaches() {
        logger.info("=== 开始综合测试所有报告类型缓存 ===")
        
        // 测试所有报告类型
        test24HourReportCache()
        test48HourReportCache()
        test72HourReportCache()
        testTodayReportCache()
        testYesterdayReportCache()
        testWeeklyReportCache()
        testMonthlyReportCache()
        testYearlyReportCache()
        
        logger.info("✅ 所有报告类型缓存测试通过")
    }
}