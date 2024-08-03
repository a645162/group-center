package com.khm.group.center.utils.time

import java.util.*

class GenshinImpactTime internal constructor(internal val date: Date) {

    companion object {
        // 北京时间凌晨4点的毫秒数偏移量
        private const val BEIJING_TIME_ZONE_OFFSET = 8 * 60 * 60 * 1000
        private const val FOUR_AM_HOUR = 4 * 60 * 60 * 1000

        // 根据当前时间创建GenshinImpactTime实例
        fun from(date: Date): GenshinImpactTime {
            return GenshinImpactTime(date)
        }
    }

    // 获取以北京时间凌晨4点为基准的日期
    private fun getNormalizedDate(): Date {
        val calendar =
            Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"))
        calendar.time = date

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millisecond = calendar.get(Calendar.MILLISECOND)

        // 计算当前时间距离当天凌晨4点的时间差
        val timeSinceFourAM = (
                hour * 60 * 60 * 1000
                        + minute * 60 * 1000
                        + second * 1000 + millisecond
                )

        // 如果当前时间早于凌晨4点，则日期减去一天
        if (timeSinceFourAM < FOUR_AM_HOUR) {
            calendar.add(Calendar.DATE, -1)
        }

        // 设置时间为凌晨4点
        calendar.set(Calendar.HOUR_OF_DAY, 4)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    infix fun isSameDay(other: GenshinImpactTime): Boolean {
        val thisNormalizedDate = getNormalizedDate().time
        val otherNormalizedDate = other.getNormalizedDate().time
        return thisNormalizedDate == otherNormalizedDate
    }

    // 比较两个GenshinImpactTime实例
    infix fun isAfter(other: GenshinImpactTime): Boolean {
        val thisNormalizedDate = getNormalizedDate().time
        val otherNormalizedDate = other.getNormalizedDate().time
        return thisNormalizedDate > otherNormalizedDate
    }

    // 重写toString方法，方便打印
    override fun toString(): String {
        return getNormalizedDate().toString()
    }
}
