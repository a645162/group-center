package com.khm.group.center.utils.cache

import java.time.LocalDateTime

/**
 * 泛型缓存数据类
 * @param T 缓存数据的类型
 * @param data 缓存的数据
 * @param timestamp 缓存时间戳（毫秒）
 * @param expiryTime 过期时间戳（毫秒）
 */
data class CachedData<T>(
    val data: T,
    val timestamp: Long,
    val expiryTime: Long
) {
    /**
     * 检查缓存是否有效
     */
    fun isValid(): Boolean {
        return System.currentTimeMillis() < expiryTime
    }
    
    /**
     * 获取缓存年龄（毫秒）
     */
    fun getAge(): Long {
        return System.currentTimeMillis() - timestamp
    }
    
    /**
     * 获取格式化后的缓存时间
     */
    fun getFormattedTimestamp(): LocalDateTime {
        return java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
    }
}