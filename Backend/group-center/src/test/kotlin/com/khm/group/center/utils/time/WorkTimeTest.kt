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
    fun testTimeRange() {
        println("Test Time:")
        println(
            DateTime.isTimeWithinRange(
                DateTime.Companion.TimeHM.getNow(),
                DateTime.Companion.TimeHM(8, 0),
                DateTime.Companion.TimeHM.parseFromString("12:00")
            )
        )
        println(
            DateTime.isTimeWithinRange(
                DateTime.Companion.TimeHM.getNow(),
                DateTime.Companion.TimeHM(10, 0),
                DateTime.Companion.TimeHM(20, 0)
            )
        )
    }
}
