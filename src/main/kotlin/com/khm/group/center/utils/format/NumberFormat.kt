package com.khm.group.center.utils.format

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 数值格式化工具类
 * 用于统一处理浮点数精度问题
 */
object NumberFormat {

    /**
     * 格式化双精度数值，保留指定小数位数
     * @param value 要格式化的数值
     * @param scale 保留的小数位数，默认为2
     * @return 格式化后的数值
     */
    fun formatDouble(value: Double, scale: Int = 2): Double {
        return if (value.isNaN() || value.isInfinite()) {
            0.0
        } else {
            BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).toDouble()
        }
    }

    /**
     * 格式化浮点数值，保留指定小数位数
     * @param value 要格式化的数值
     * @param scale 保留的小数位数，默认为2
     * @return 格式化后的数值
     */
    fun formatFloat(value: Float, scale: Int = 2): Float {
        return if (value.isNaN() || value.isInfinite()) {
            0.0f
        } else {
            BigDecimal(value.toDouble()).setScale(scale, RoundingMode.HALF_UP).toFloat()
        }
    }

    /**
     * 格式化百分比数值，保留指定小数位数
     * @param value 要格式化的百分比数值（0-100）
     * @param scale 保留的小数位数，默认为2
     * @return 格式化后的百分比数值
     */
    fun formatPercentage(value: Double, scale: Int = 2): Double {
        return formatDouble(value, scale)
    }

    /**
     * 计算并格式化平均值
     * @param total 总值
     * @param count 数量
     * @param scale 保留的小数位数，默认为2
     * @return 格式化后的平均值
     */
    fun formatAverage(total: Double, count: Int, scale: Int = 2): Double {
        return if (count > 0) {
            formatDouble(total / count, scale)
        } else {
            0.0
        }
    }

    /**
     * 计算并格式化加权平均值
     * @param currentAverage 当前平均值
     * @param currentCount 当前数量
     * @param newValue 新值
     * @param scale 保留的小数位数，默认为2
     * @return 格式化后的加权平均值
     */
    fun formatWeightedAverage(currentAverage: Double, currentCount: Int, newValue: Double, scale: Int = 2): Double {
        return if (currentCount > 0) {
            formatDouble((currentAverage * currentCount + newValue) / (currentCount + 1), scale)
        } else {
            formatDouble(newValue, scale)
        }
    }
}