package com.khm.group.center.datatype.utils.common

class FloatValue {
    companion object {
        fun round(floatValue: Float, count: Int = 2): String {
            return String.format("%.${count}f", floatValue)
        }
    }
}
