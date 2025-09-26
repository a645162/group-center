package com.khm.group.center.utils.time

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 整点时间计算工具类
 * 用于计算24/48/72小时报告的整点时间范围
 */
object RoundedHourUtils {
    
    /**
     * 获取当前时间的整点小时（向上取整）
     * 例如：14:10 -> 15:00
     * @return 整点小时（0-23）
     */
    fun getRoundedHour(): Int {
        val now = LocalDateTime.now()
        return if (now.minute > 0 || now.second > 0 || now.nano > 0) {
            (now.hour + 1) % 24
        } else {
            now.hour
        }
    }
    
    /**
     * 获取指定小时数的整点时间范围
     * 例如：现在是14:10，计算24小时报告的时间范围是昨天15:00到今天15:00
     * @param hours 小时数（24/48/72）
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getRoundedHourRange(hours: Int): Pair<Long, Long> {
        val roundedHour = getRoundedHour()
        return getRoundedHourRange(hours, roundedHour)
    }
    
    /**
     * 获取指定小时数和整点小时的整点时间范围
     * @param hours 小时数
     * @param roundedHour 整点小时
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getRoundedHourRange(hours: Int, roundedHour: Int): Pair<Long, Long> {
        val now = LocalDateTime.now()
        
        // 计算结束时间（今天的整点小时）
        val endTime = now
            .withHour(roundedHour)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        // 如果当前时间已经过了整点小时，结束时间应该是明天的整点小时
        val adjustedEndTime = if (now.isAfter(endTime)) {
            endTime.plusDays(1)
        } else {
            endTime
        }
        
        // 计算开始时间（结束时间往前推指定小时数）
        val startTime = adjustedEndTime.minusHours(hours.toLong())
        
        // 转换为时间戳（秒）
        val startTimestamp = startTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = adjustedEndTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTimestamp, endTimestamp)
    }
    
    /**
     * 获取今日报告的整点时间范围（今天0:00到明天0:00）
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getTodayRoundedRange(): Pair<Long, Long> {
        val today = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        val tomorrow = today.plusDays(1)
        
        val startTimestamp = today.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = tomorrow.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTimestamp, endTimestamp)
    }
    
    /**
     * 获取昨日报告的整点时间范围（昨天0:00到今天0:00）
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getYesterdayRoundedRange(): Pair<Long, Long> {
        val yesterday = LocalDateTime.now()
            .minusDays(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        val today = yesterday.plusDays(1)
        
        val startTimestamp = yesterday.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = today.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTimestamp, endTimestamp)
    }
    
    /**
     * 获取上周的整点时间范围（上周一0:00到本周一0:00）
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getWeeklyRoundedRange(): Pair<Long, Long> {
        val now = LocalDateTime.now()
        
        // 找到本周一
        val currentMonday = now.with(java.time.DayOfWeek.MONDAY)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        // 上周一
        val lastMonday = currentMonday.minusWeeks(1)
        
        val startTimestamp = lastMonday.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = currentMonday.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTimestamp, endTimestamp)
    }
    
    /**
     * 获取上月的整点时间范围（上月1号0:00到本月1号0:00）
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getMonthlyRoundedRange(): Pair<Long, Long> {
        val now = LocalDateTime.now()
        
        // 本月1号
        val currentMonthFirstDay = now.withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        // 上月1号
        val lastMonthFirstDay = currentMonthFirstDay.minusMonths(1)
        
        val startTimestamp = lastMonthFirstDay.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = currentMonthFirstDay.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTimestamp, endTimestamp)
    }
    
    /**
     * 获取去年的整点时间范围（去年1月1号0:00到今年1月1号0:00）
     * @return Pair(开始时间戳, 结束时间戳)（秒）
     */
    fun getYearlyRoundedRange(): Pair<Long, Long> {
        val now = LocalDateTime.now()
        
        // 今年1月1号
        val currentYearFirstDay = now.withDayOfYear(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        // 去年1月1号
        val lastYearFirstDay = currentYearFirstDay.minusYears(1)
        
        val startTimestamp = lastYearFirstDay.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = currentYearFirstDay.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        return Pair(startTimestamp, endTimestamp)
    }
    
    /**
     * 检查当前时间是否是整点（分钟、秒、纳秒都为0）
     * @return 是否是整点
     */
    fun isRoundedHour(): Boolean {
        val now = LocalDateTime.now()
        return now.minute == 0 && now.second == 0 && now.nano == 0
    }
    
    /**
     * 获取距离下一个整点还有多少秒
     * @return 秒数
     */
    fun getSecondsToNextRoundedHour(): Long {
        val now = LocalDateTime.now()
        val nextHour = now.plusHours(1)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        
        return ChronoUnit.SECONDS.between(now, nextHour)
    }
    
    /**
     * 获取24/48/72小时报告的描述信息
     * @param hours 小时数
     * @param roundedHour 整点小时
     * @return 描述信息
     */
    fun getHourlyReportDescription(hours: Int, roundedHour: Int): String {
        val (startTime, endTime) = getRoundedHourRange(hours, roundedHour)
        val startDateTime = LocalDateTime.ofEpochSecond(startTime, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.ofEpochSecond(startTime)))
        val endDateTime = LocalDateTime.ofEpochSecond(endTime, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.ofEpochSecond(endTime)))
        
        return "${hours}小时报告（${startDateTime} - ${endDateTime}）"
    }
}