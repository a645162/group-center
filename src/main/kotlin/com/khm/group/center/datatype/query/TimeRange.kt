package com.khm.group.center.datatype.query

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 时间范围
 */
data class TimeRange(
    val startTime: Instant? = null,
    val endTime: Instant? = null
) {
    /**
     * 验证时间范围的有效性
     */
    fun validate(): Boolean {
        if (startTime != null && endTime != null) {
            return !startTime.isAfter(endTime)
        }
        return true
    }

    /**
     * 获取开始时间的Unix时间戳（秒）
     */
    fun getStartTimestamp(): Long? {
        return startTime?.epochSecond
    }

    /**
     * 获取结束时间的Unix时间戳（秒）
     */
    fun getEndTimestamp(): Long? {
        return endTime?.epochSecond
    }

    /**
     * 从字符串创建时间范围
     */
    companion object {
        private val formatter = DateTimeFormatter.ISO_DATE_TIME

        fun fromStrings(startTimeStr: String?, endTimeStr: String?): TimeRange {
            val startTime = startTimeStr?.let {
                LocalDateTime.parse(it, formatter)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            }
            
            val endTime = endTimeStr?.let {
                LocalDateTime.parse(it, formatter)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            }
            
            return TimeRange(startTime, endTime)
        }

        /**
         * 创建最近N小时的时间范围
         */
        fun lastNHours(hours: Int): TimeRange {
            val endTime = Instant.now()
            val startTime = endTime.minusSeconds(hours.toLong() * 3600)
            return TimeRange(startTime, endTime)
        }

        /**
         * 创建最近N天的时间范围
         */
        fun lastNDays(days: Int): TimeRange {
            val endTime = Instant.now()
            val startTime = endTime.minusSeconds(days.toLong() * 24 * 3600)
            return TimeRange(startTime, endTime)
        }
    }
}