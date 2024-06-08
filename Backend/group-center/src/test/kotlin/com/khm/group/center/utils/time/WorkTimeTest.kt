package com.khm.group.center.utils.time

import com.khm.group.center.datatype.utils.datetime.DateTime
import com.khm.group.center.datatype.utils.datetime.TimeHM
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
        println(TimeHM.getNow().toString())
        println(TimeHM(8, 0))
        println(TimeHM.parseFromString("12:00"))
    }

    @Test
    fun testTimeRangeOneDay() {
        println("Test Time Range One Day")

        val startHour = 8
        val endHour = 20
        val startTime = TimeHM(startHour, 0)
        val endTime = TimeHM(endHour, 0)
        for (hour in 0..23) {
            val time = TimeHM(hour, 0)
            val hourInRange = hour in startHour..endHour
            assert(hourInRange == TimeHM.isTimeWithinRange(time, startTime, endTime))
        }
    }

    @Test
    fun testTimeRangeTwoDay() {
        println("Test Time Range Two Day")

        val startHour = 20
        val endHour = 8
        val startTime = TimeHM(startHour, 0)
        val endTime = TimeHM(endHour, 0)
        for (hour in 0..23) {
            val time = TimeHM(hour, 0)
            val hourInRange = hour in startHour..23 || hour in 0..endHour
            assert(hourInRange == TimeHM.isTimeWithinRange(time, startTime, endTime))
        }

        assert(
            TimeHM.isTimeWithinRange(
                TimeHM(23, 59),
                TimeHM(20, 0),
                TimeHM(2, 0)
            )
        )

        assert(
            TimeHM.isTimeWithinRange(
                TimeHM(0, 1),
                TimeHM(20, 0),
                TimeHM(2, 0)
            )
        )
    }
}
