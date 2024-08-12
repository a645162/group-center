package com.khm.group.center.utils.time

import org.junit.jupiter.api.Test
import java.util.*

class GenshinTimeTest {
    @Test
    fun test() {
        val date1 = Date()
        // 明天的同一时间
        val date2 = Date(System.currentTimeMillis() + (24 - 0) * 60 * 60 * 1000)

        val time1 = GenshinImpactTime.from(date1)
        val time2 = GenshinImpactTime.from(date2)

        println("Time1: $date1")
        println("Time2: $date1")
        println(date1 < date2)
        println("Time1 isSameDay Time2: ${time1 isSameDay time2}")

        assert(!(time1 isSameDay time2))
    }
}
