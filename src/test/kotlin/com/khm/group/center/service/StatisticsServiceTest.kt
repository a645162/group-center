package com.khm.group.center.service

import com.khm.group.center.utils.time.TimeAnalysisUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.LocalTime

class StatisticsServiceTest {

    private val logger = LoggerFactory.getLogger(StatisticsServiceTest::class.java)

    @Test
    fun testTimeAnalysisUtils() {
        logger.info("测试时间分析工具类")
        
        val timeAnalysisUtils = TimeAnalysisUtils()
        
        // 测试熬夜时间判断
        val lateNightTimestamp = LocalDateTime.of(2025, 9, 25, 2, 30)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        assertTrue(timeAnalysisUtils.isLateNight(lateNightTimestamp), "凌晨2:30应该是熬夜时间")
        
        // 测试早起时间判断
        val earlyMorningTimestamp = LocalDateTime.of(2025, 9, 25, 6, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        assertTrue(timeAnalysisUtils.isEarlyMorning(earlyMorningTimestamp), "早上6:00应该是早起时间")
        
        // 测试正常时间判断
        val normalTimestamp = LocalDateTime.of(2025, 9, 25, 14, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        assertFalse(timeAnalysisUtils.isLateNight(normalTimestamp), "下午2:00不应该是熬夜时间")
        assertFalse(timeAnalysisUtils.isEarlyMorning(normalTimestamp), "下午2:00不应该是早起时间")
        
        logger.info("时间分析工具类测试通过")
    }
    @Test
    fun testTimeAnalysisUtilsMethods() {
        logger.info("测试时间分析工具类方法")
        
        // 创建测试用的时间分析工具实例
        val timeAnalysisUtils = TimeAnalysisUtils()
        
        // 测试熬夜时间判断
        val lateNightTimestamp = LocalDateTime.of(2025, 9, 25, 2, 30)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        assertTrue(timeAnalysisUtils.isLateNight(lateNightTimestamp), "凌晨2:30应该是熬夜时间")
        
        // 测试早起时间判断
        val earlyMorningTimestamp = LocalDateTime.of(2025, 9, 25, 6, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        assertTrue(timeAnalysisUtils.isEarlyMorning(earlyMorningTimestamp), "早上6:00应该是早起时间")
        
        // 测试正常时间判断
        val normalTimestamp = LocalDateTime.of(2025, 9, 25, 14, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        assertFalse(timeAnalysisUtils.isLateNight(normalTimestamp), "下午2:00不应该是熬夜时间")
        assertFalse(timeAnalysisUtils.isEarlyMorning(normalTimestamp), "下午2:00不应该是早起时间")
        
        logger.info("时间分析工具类方法测试通过")
    }

    @Test
    fun testTimeBoundaries() {
        logger.info("测试时间边界条件")
        
        val timeAnalysisUtils = TimeAnalysisUtils()
        
        // 测试熬夜边界（00:00-04:00）
        val midnight = LocalDateTime.of(2025, 9, 25, 0, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        val before4am = LocalDateTime.of(2025, 9, 25, 3, 59)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        val at4am = LocalDateTime.of(2025, 9, 25, 4, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        
        assertTrue(timeAnalysisUtils.isLateNight(midnight), "00:00应该是熬夜时间")
        assertTrue(timeAnalysisUtils.isLateNight(before4am), "03:59应该是熬夜时间")
        assertFalse(timeAnalysisUtils.isLateNight(at4am), "04:00不应该是熬夜时间")
        
        // 测试早起边界（04:00-10:00）
        val after4am = LocalDateTime.of(2025, 9, 25, 4, 1)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        val before10am = LocalDateTime.of(2025, 9, 25, 9, 59)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        val at10am = LocalDateTime.of(2025, 9, 25, 10, 0)
            .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        
        assertTrue(timeAnalysisUtils.isEarlyMorning(after4am), "04:01应该是早起时间")
        assertTrue(timeAnalysisUtils.isEarlyMorning(before10am), "09:59应该是早起时间")
        assertFalse(timeAnalysisUtils.isEarlyMorning(at10am), "10:00不应该是早起时间")
        
        logger.info("时间边界条件测试通过")
    }
}