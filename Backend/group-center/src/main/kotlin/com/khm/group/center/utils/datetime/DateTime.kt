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

    }

}