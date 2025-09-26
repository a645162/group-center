package com.khm.group.center.utils.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日期时间工具类
 * 提供统一的时间戳转换和格式化功能
 */
object DateTimeUtils {

    /**
     * 将秒级时间戳转换为 LocalDateTime
     */
    fun convertTimestampToDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp),
            ZoneId.systemDefault()
        )
    }

    /**
     * 格式化日期时间为完整字符串
     */
    fun formatDateTimeFull(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    /**
     * 格式化日期时间为短格式（用于报告显示）
     */
    fun formatDateTimeShort(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    }

    /**
     * 获取当前时间的秒级时间戳
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    /**
     * 验证时间戳转换是否正确（按年月日验证）
     */
    fun validateTimestampConversion(timestamp: Long, expectedYear: Int, expectedMonth: Int, expectedDay: Int): Boolean {
        val dateTime = convertTimestampToDateTime(timestamp)
        return dateTime.year == expectedYear &&
                dateTime.monthValue == expectedMonth &&
                dateTime.dayOfMonth == expectedDay
    }

    /**
     * 验证时间戳转换是否正确（按完整时间字符串验证）
     */
    fun validateTimestampConversion(timestamp: Long, expectedDateTimeString: String): Boolean {
        val dateTime = convertTimestampToDateTime(timestamp)
        val formatted = formatDateTimeFull(dateTime)
        return formatted == expectedDateTimeString
    }
}