package com.khm.group.center.datatype.utils.datetime

import com.khm.group.center.datatype.utils.datetime.DateTime.Companion.getNowCalendar
import java.util.*

class TimeHM(var hour: Int = 0, var minute: Int = 0) {
    override fun toString(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    companion object {
        fun getNow(): TimeHM {
            val now = getNowCalendar()
            return TimeHM(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
        }

        fun parseFromString(text: String): TimeHM {
            val obj = TimeHM()

            val str = text.trim()

            // Spilt
            val parts = str.split(":")
            if (parts.size == 2) {
                obj.hour = parts[0].toInt()
                obj.minute = parts[1].toInt()
            }

            return obj
        }

        fun isTimeWithinRange(
            now: TimeHM,
            startTime: TimeHM,
            endTime: TimeHM
        ): Boolean {

            val start = startTime.hour * 60 + startTime.minute
            val end = endTime.hour * 60 + endTime.minute
            val current = now.hour * 60 + now.minute

            return if (start < end) {
                current in start..end
            } else {
                current in start..<24 * 60 || current in 0..end
            }
        }
    }
}
