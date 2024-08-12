package com.khm.group.center.utils.time

import java.util.*
import kotlin.time.times

enum class TimePeriod {
    ONE_WEEK,
    ONE_MONTH,
    SIX_MONTH,
    ONE_YEAR,
    THREE_YEAR,
    ALL;

    fun getDisplayName(): String {
        val timePeriod = this

        return when (timePeriod) {
            ONE_WEEK -> "One Week"
            ONE_MONTH -> "One Month"
            SIX_MONTH -> "Six Months"
            ONE_YEAR -> "One Year"
            THREE_YEAR -> "Three Years"
            ALL -> "All Time"
        }
    }

    fun getAgoTime(currentTime: Date?): Date {
        val timePeriod = this
        val finalCurrentTime = currentTime ?: Date()

        return when (timePeriod) {
            ONE_WEEK -> TimeAgo.getOneWeekAgoDate(finalCurrentTime)
            ONE_MONTH -> TimeAgo.getOneMonthAgoDate(finalCurrentTime)
            SIX_MONTH -> TimeAgo.getSixMonthAgoDate(finalCurrentTime)
            ONE_YEAR -> TimeAgo.getOneYearAgoDate(finalCurrentTime)
            THREE_YEAR -> TimeAgo.getThreeYearAgoDate(finalCurrentTime)
            ALL -> Date(0)
        }
    }

    fun getAgoTimestamp(currentTime: Date?): Long {
        return getAgoTime(currentTime).time
    }

    fun getHours(): Int {
        val timePeriod = this

        return when (timePeriod) {
            ONE_WEEK -> 24 * 7
            ONE_MONTH -> 24 * 30
            SIX_MONTH -> 24 * 30 * 6
            ONE_YEAR -> 24 * 365
            THREE_YEAR -> 24 * 365 * 3
            ALL -> 0
        }
    }

    fun getMinutes(): Long {
        return (getHours() as Long) * 60
    }
}
