package com.khm.group.center.utils.datetime

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.TimeZone.getTimeZone

class DateTime {

    companion object {

        fun getDateTimeStrByPythonTimeStamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = Date(timestamp * 1000)
            return sdf.format(date)
        }

        fun getCurrentTimestamp(): Long {
            return System.currentTimeMillis()
        }

        fun getCurrentDateTimeStr(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = Date()
            return sdf.format(date)
        }

        fun getCurrentExpireDateTimeStr(expireTime: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
            val date = Date(System.currentTimeMillis() + expireTime)
            return sdf.format(date)
        }

        fun getTimestampFromExpireDateTime(expireTimeStr: String): Long {
            val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
            val date = sdf.parse(expireTimeStr)
            return date.time
        }

        fun isDatetimeWithinRange(dateTime: Calendar, startTime: Calendar, endTime: Calendar): Boolean {
            return dateTime.timeInMillis in startTime.timeInMillis until endTime.timeInMillis
        }

        fun getMonthName(month: Int): String {
            return when (month) {
                0 -> "January"
                1 -> "February"
                2 -> "March"
                3 -> "April"
                4 -> "May"
                5 -> "June"
                6 -> "July"
                7 -> "August"
                8 -> "September"
                9 -> "October"
                10 -> "November"
                11 -> "December"
                else -> ""
            }
        }

        fun getMonthName(calendar: Calendar): String {
            val month = calendar.get(Calendar.MONTH)
            return getMonthName(month)
        }

        fun printCalendar(calendarObj: Calendar) {
            println("DateTime:")
            println("\tYear: ${calendarObj.get(Calendar.YEAR)}")
            println("\tMonth: ${getMonthName(calendarObj.get(Calendar.MONTH))}")
            println("\tDay: ${calendarObj.get(Calendar.DATE)}")
            println("\tHour: ${calendarObj.get(Calendar.HOUR)}")
            println("\tMinute: ${calendarObj.get(Calendar.MINUTE)}")
            println("\tSecond: ${calendarObj.get(Calendar.SECOND)}")
            println("\tMilliseconds: ${calendarObj.get(Calendar.MILLISECOND)}")
            println("\tTime Zone: ${calendarObj.getTimeZone().displayName}")
        }

        /* Month start with 1 */
        fun getCalendarByDateTime(
            year: Int, month: Int, day: Int,
            hour: Int, minute: Int, second: Int
        ): Calendar {
            val calendarObj = Calendar.getInstance()
            calendarObj.set(
                year, month + 1, day,
                hour, minute, second
            )
            return calendarObj
        }

        fun getNowCalendar(): Calendar {
            return Calendar.getInstance(getTimeZone("Asia/Shanghai"))
        }

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
            }
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
