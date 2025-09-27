package com.khm.group.center.utils.time

import java.util.*

class TimeAgo {
    companion object{
        fun getOneYearAgoDate(currentTime: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = currentTime
            calendar.add(Calendar.YEAR, -1)
            return calendar.time
        }

        fun getThreeYearAgoDate(currentTime: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = currentTime
            calendar.add(Calendar.YEAR, -3)
            return calendar.time
        }

        fun getOneMonthAgoDate(currentTime: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = currentTime
            calendar.add(Calendar.MONTH, -1)
            return calendar.time
        }

        fun getSixMonthAgoDate(currentTime: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = currentTime
            calendar.add(Calendar.MONTH, -6)
            return calendar.time
        }

        fun getOneDayAgoDate(currentTime: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = currentTime
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

        fun getOneWeekAgoDate(currentTime: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = currentTime
            calendar.add(Calendar.DATE, -7)
            return calendar.time
        }
    }
}
