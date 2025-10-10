package com.khm.group.center.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 时间区间合并器
 * 用于合并多个时间区间，支持跨天情况的特殊处理
 */
class TimeRangeMerger {
    
    /**
     * 时间区间
     */
    data class TimeRange(
        val start: LocalDateTime,
        val end: LocalDateTime
    ) {
        /**
         * 转换为分钟表示（从当天0点开始计算）
         */
        fun toMinutes(): Pair<Int, Int> {
            val startMinutes = start.hour * 60 + start.minute
            val endMinutes = end.hour * 60 + end.minute
            
            // 如果结束时间在开始时间之前，说明跨天了
            val adjustedEndMinutes = if (endMinutes < startMinutes) {
                endMinutes + 24 * 60  // 加上一天的分钟数
            } else {
                endMinutes
            }
            
            return Pair(startMinutes, adjustedEndMinutes)
        }
        
        /**
         * 从分钟转换回时间字符串
         */
        fun fromMinutes(startMinutes: Int, endMinutes: Int): Pair<String, String> {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            
            val startHour = startMinutes / 60
            val startMinute = startMinutes % 60
            val startTime = LocalDateTime.of(start.toLocalDate(), java.time.LocalTime.of(startHour, startMinute))
            
            // 处理跨天情况
            val adjustedEndMinutes = if (endMinutes >= 24 * 60) endMinutes - 24 * 60 else endMinutes
            val endHour = adjustedEndMinutes / 60
            val endMinute = adjustedEndMinutes % 60
            val endTime = LocalDateTime.of(start.toLocalDate(), java.time.LocalTime.of(endHour, endMinute))
            
            return Pair(startTime.format(timeFormatter), endTime.format(timeFormatter))
        }
        
        /**
         * 检查是否跨天
         */
        fun isCrossDay(): Boolean {
            val (_, endMinutes) = toMinutes()
            return endMinutes >= 24 * 60
        }
    }
    
    /**
     * 合并多个时间区间
     * 算法：将所有时间区间转换为分钟表示，然后取最小开始时间和最大结束时间
     * 支持跨天情况的特殊处理
     */
    fun mergeTimeRanges(ranges: List<TimeRange>): TimeRange? {
        if (ranges.isEmpty()) return null
        
        // 将所有时间区间转换为分钟表示
        val minuteRanges = ranges.map { it.toMinutes() }
        
        // 找到最小的开始时间和最大的结束时间
        val minStart = minuteRanges.minOf { it.first }
        val maxEnd = minuteRanges.maxOf { it.second }
        
        // 使用第一个区间的时间作为基准日期
        val baseDate = ranges.first().start.toLocalDate()
        DateTimeFormatter.ofPattern("HH:mm")
        
        // 转换回时间
        val startHour = minStart / 60
        val startMinute = minStart % 60
        val startTime = LocalDateTime.of(baseDate, java.time.LocalTime.of(startHour, startMinute))
        
        // 处理跨天情况
        val adjustedEndMinutes = if (maxEnd >= 24 * 60) maxEnd - 24 * 60 else maxEnd
        val endHour = adjustedEndMinutes / 60
        val endMinute = adjustedEndMinutes % 60
        val endTime = LocalDateTime.of(baseDate, java.time.LocalTime.of(endHour, endMinute))
        
        return TimeRange(startTime, endTime)
    }
    
    /**
     * 格式化时间区间为字符串
     */
    fun formatTimeRange(range: TimeRange): String {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val startStr = range.start.format(timeFormatter)
        val endStr = range.end.format(timeFormatter)
        
        return if (range.isCrossDay()) {
            "$startStr-次日$endStr"
        } else {
            "$startStr-$endStr"
        }
    }
    
    /**
     * 从LocalDateTime创建时间区间
     */
    fun createRange(start: LocalDateTime, end: LocalDateTime): TimeRange {
        return TimeRange(start, end)
    }
    
    /**
     * 从字符串解析时间区间
     */
    fun parseRange(startStr: String, endStr: String, baseDate: java.time.LocalDate): TimeRange {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val startTime = java.time.LocalTime.parse(startStr, timeFormatter)
        val endTime = java.time.LocalTime.parse(endStr, timeFormatter)
        
        val startDateTime = LocalDateTime.of(baseDate, startTime)
        val endDateTime = LocalDateTime.of(baseDate, endTime)
        
        return TimeRange(startDateTime, endDateTime)
    }
}