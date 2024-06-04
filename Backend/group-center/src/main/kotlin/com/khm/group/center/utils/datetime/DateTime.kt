package com.khm.group.center.utils.datetime

import java.text.SimpleDateFormat
import java.util.*

class DateTime {

    companion object {

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

    }

}