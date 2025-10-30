package com.khm.group.center.utils.enum

import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger

/**
 * 安全的枚举转换工具类
 * 提供安全的字符串到枚举值的转换，避免 IllegalArgumentException 异常
 */
object EnumUtils {

    /**
     * 安全地将字符串转换为枚举值
     * @param value 要转换的字符串
     * @param enumClass 枚举类
     * @param defaultValue 默认值，当转换失败时返回
     * @return 转换后的枚举值，如果转换失败则返回默认值
     */
    inline fun <reified T : Enum<T>> safeValueOf(
        value: String?,
        defaultValue: T
    ): T {
        return try {
            if (value.isNullOrBlank()) {
                logger.warn("Enum safeValueOf: input value is null or blank, using default value: ${defaultValue.name}")
                return defaultValue
            }
            
            enumValueOf<T>(value.trim())
        } catch (e: IllegalArgumentException) {
            logger.warn("Enum safeValueOf: Invalid enum value '$value' for ${T::class.java.simpleName}, using default value: ${defaultValue.name}")
            defaultValue
        } catch (e: Exception) {
            logger.error("Enum safeValueOf: Unexpected error when converting '$value' to ${T::class.java.simpleName}: ${e.message}", e)
            defaultValue
        }
    }

    /**
     * 安全地将字符串转换为枚举值，支持大小写不敏感和下划线/连字符转换
     * @param value 要转换的字符串
     * @param enumClass 枚举类
     * @param defaultValue 默认值，当转换失败时返回
     * @return 转换后的枚举值，如果转换失败则返回默认值
     */
    inline fun <reified T : Enum<T>> safeValueOfFlexible(
        value: String?,
        defaultValue: T
    ): T {
        return try {
            if (value.isNullOrBlank()) {
                logger.warn("Enum safeValueOfFlexible: input value is null or blank, using default value: ${defaultValue.name}")
                return defaultValue
            }

            val normalizedValue = value.trim()
                .uppercase()
                .replace("-", "_") // 将连字符转换为下划线
                .replace(" ", "_") // 将空格转换为下划线

            // 首先尝试直接转换
            try {
                enumValueOf<T>(normalizedValue)
            } catch (e: IllegalArgumentException) {
                // 如果直接转换失败，尝试在枚举值中查找匹配的
                val enumValues = enumValues<T>()
                val matchedEnum = enumValues.find { enum ->
                    enum.name.equals(normalizedValue, ignoreCase = true) ||
                    enum.name.replace("_", "").equals(normalizedValue.replace("_", ""), ignoreCase = true)
                }
                
                matchedEnum ?: throw IllegalArgumentException("No matching enum value found")
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Enum safeValueOfFlexible: Invalid enum value '$value' for ${T::class.java.simpleName}, using default value: ${defaultValue.name}")
            defaultValue
        } catch (e: Exception) {
            logger.error("Enum safeValueOfFlexible: Unexpected error when converting '$value' to ${T::class.java.simpleName}: ${e.message}", e)
            defaultValue
        }
    }

    /**
     * 获取枚举值的名称列表
     * @param enumClass 枚举类
     * @return 枚举值名称列表
     */
    inline fun <reified T : Enum<T>> getEnumNames(): List<String> {
        return enumValues<T>().map { it.name }
    }

    /**
     * 检查字符串是否是有效的枚举值
     * @param value 要检查的字符串
     * @param enumClass 枚举类
     * @return 如果是有效的枚举值则返回 true
     */
    inline fun <reified T : Enum<T>> isValidEnumValue(value: String?): Boolean {
        return try {
            if (value.isNullOrBlank()) return false
            enumValueOf<T>(value.trim())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * 检查字符串是否是有效的枚举值（灵活模式）
     * @param value 要检查的字符串
     * @param enumClass 枚举类
     * @return 如果是有效的枚举值则返回 true
     */
    inline fun <reified T : Enum<T>> isValidEnumValueFlexible(value: String?): Boolean {
        return try {
            if (value.isNullOrBlank()) return false
            
            val normalizedValue = value.trim()
                .uppercase()
                .replace("-", "_")
                .replace(" ", "_")

            try {
                enumValueOf<T>(normalizedValue)
                true
            } catch (e: IllegalArgumentException) {
                val enumValues = enumValues<T>()
                enumValues.any { enum ->
                    enum.name.equals(normalizedValue, ignoreCase = true) ||
                    enum.name.replace("_", "").equals(normalizedValue.replace("_", ""), ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}