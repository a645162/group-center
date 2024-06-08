package com.khm.group.center.utils.time

import com.khm.group.center.utils.datetime.DateTime
import org.junit.jupiter.api.Test

class WorkTimeTest {
    @Test
    fun testDateTime() {
        val now = DateTime.getNowCalendar()
        DateTime.printCalendar(now)

        val startDate = DateTime.getCalendarByDateTime(
            2024, 1, 1,
            10, 0, 0
        )
        DateTime.printCalendar(startDate)

        val endDate = DateTime.getCalendarByDateTime(
            2025, 1, 1,
            12, 0, 0
        )
        DateTime.printCalendar(endDate)

        val result = DateTime.isDatetimeWithinRange(
            now,
            startDate,
            endDate
        )

        println("isDatetimeWithinRange: $result")
    }

    @Test
    fun testTimeParse() {
        println(DateTime.Companion.TimeHM.getNow().toString())
        println(DateTime.Companion.TimeHM(8, 0))
        println(DateTime.Companion.TimeHM.parseFromString("12:00"))
    }

    @Test
    fun testTimeRangeOneDay() {
        println("Test Time Range One Day")

        val startHour = 8
        val endHour = 20
        val startTime = DateTime.Companion.TimeHM(startHour, 0)
        val endTime = DateTime.Companion.TimeHM(endHour, 0)
        for (hour in 0..23) {
            val time = DateTime.Companion.TimeHM(hour, 0)
            val hourInRange = hour in startHour..endHour
            assert(hourInRange == DateTime.isTimeWithinRange(time, startTime, endTime))
        }
    }

    @Test
    fun testTimeRangeTwoDay() {
        println("Test Time Range Two Day")

        val startHour = 20
        val endHour = 8
        val startTime = DateTime.Companion.TimeHM(startHour, 0)
        val endTime = DateTime.Companion.TimeHM(endHour, 0)
        for (hour in 0..23) {
            val time = DateTime.Companion.TimeHM(hour, 0)
            val hourInRange = hour in startHour..23 || hour in 0..endHour
            assert(hourInRange == DateTime.isTimeWithinRange(time, startTime, endTime))
        }

        assert(
            DateTime.isTimeWithinRange(
                DateTime.Companion.TimeHM(23, 59),
                DateTime.Companion.TimeHM(20, 0),
                DateTime.Companion.TimeHM(2, 0)
            )
        )

        assert(
            DateTime.isTimeWithinRange(
                DateTime.Companion.TimeHM(0, 1),
                DateTime.Companion.TimeHM(20, 0),
                DateTime.Companion.TimeHM(2, 0)
            )
        )
    }
}
