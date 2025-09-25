package com.khm.group.center.utils.time

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 整小时时间工具类
 * 用于处理向后取整的整小时时间计算
 */
object HourlyTimeUtils {

    /**
     * 获取当前时间的整小时向后取整时间
     * 例如：14:10 -> 15:00
     * @return 向后取整的整小时时间戳（秒）
     */
    fun getRoundedHourTimestamp(): Long {
        val now = LocalDateTime.now()
        val roundedHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        return roundedHour.atZone(ZoneId.systemDefault()).toEpochSecond()
    }

    /**
     * 获取指定小时数的向后取整时间范围
     * 例如：现在是14:10，统计24小时 -> 昨天15:00到今天15:00
     * @param hours 小时数
     * @return Pair(开始时间戳, 结束时间戳) 单位：秒
     */
    fun getRoundedHourRange(hours: Int): Pair<Long, Long> {
        val endTime = getRoundedHourTimestamp()
        val startTime = endTime - hours * 60 * 60
        return Pair(startTime, endTime)
    }

    /**
     * 获取今日日报的整点时间范围（今天0:00到明天0:00）
     * @return Pair(开始时间戳, 结束时间戳) 单位：秒
     */
    fun getTodayRoundedRange(): Pair<Long, Long> {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val tomorrow = today.plusDays(1)
        
        val startTime = today.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTime = tomorrow.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTime, endTime)
    }

    /**
     * 获取昨日日报的整点时间范围（昨天0:00到今天0:00）
     * @return Pair(开始时间戳, 结束时间戳) 单位：秒
     */
    fun getYesterdayRoundedRange(): Pair<Long, Long> {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val yesterday = today.minusDays(1)
        
        val startTime = yesterday.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTime = today.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTime, endTime)
    }

    /**
     * 获取周报的整点时间范围（上周一0:00到本周一0:00）
     * @return Pair(开始时间戳, 结束时间戳) 单位：秒
     */
    fun getWeeklyRoundedRange(): Pair<Long, Long> {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val thisMonday = today.with(java.time.DayOfWeek.MONDAY)
        val lastMonday = thisMonday.minusWeeks(1)
        
        val startTime = lastMonday.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTime = thisMonday.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTime, endTime)
    }

    /**
     * 获取月报的整点时间范围（上月1号0:00到本月1号0:00）
     * @return Pair(开始时间戳, 结束时间戳) 单位：秒
     */
    fun getMonthlyRoundedRange(): Pair<Long, Long> {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val thisMonthFirst = today.withDayOfMonth(1)
        val lastMonthFirst = thisMonthFirst.minusMonths(1)
        
        val startTime = lastMonthFirst.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTime = thisMonthFirst.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTime, endTime)
    }

    /**
     * 获取年报的整点时间范围（去年1月1号0:00到今年1月1号0:00）
     * @return Pair(开始时间戳, 结束时间戳) 单位：秒
     */
    fun getYearlyRoundedRange(): Pair<Long, Long> {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val thisYearFirst = today.withDayOfYear(1)
        val lastYearFirst = thisYearFirst.minusYears(1)
        
        val startTime = lastYearFirst.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTime = thisYearFirst.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTime, endTime)
    }

    /**
     * 获取当前小时数（用于缓存键）
     * @return 当前小时数（从1970年开始的小时数）
     */
    fun getCurrentHour(): Long {
        return System.currentTimeMillis() / (60 * 60 * 1000)
    }
}