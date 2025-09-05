package com.khm.group.center.utils.cache

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 类型安全的缓存管理器
 * 使用泛型确保类型安全，避免未经检查的类型转换
 */
@Component
class CacheManager {

    private val cache = ConcurrentHashMap<String, CachedData<*>>()

    /**
     * 获取缓存数据
     * @param key 缓存键
     * @return 缓存数据，如果不存在或已过期则返回null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedData(key: String): T? {
        val cached = cache[key] as? CachedData<T>
        return if (cached != null && cached.isValid()) {
            cached.data
        } else {
            // 移除过期缓存
            if (cached != null && !cached.isValid()) {
                cache.remove(key)
            }
            null
        }
    }

    /**
     * 存储缓存数据
     * @param key 缓存键
     * @param data 要缓存的数据
     * @param expiryMillis 过期时间（毫秒）
     */
    fun <T> putCachedData(key: String, data: T, expiryMillis: Long) {
        val cachedData = CachedData(
            data = data,
            timestamp = System.currentTimeMillis(),
            expiryTime = System.currentTimeMillis() + expiryMillis
        )
        cache[key] = cachedData
    }

    /**
     * 存储缓存数据（使用默认过期时间）
     * @param key 缓存键
     * @param data 要缓存的数据
     */
    fun <T> putCachedData(key: String, data: T) {
        putCachedData(key, data, DEFAULT_EXPIRY_MILLIS)
    }

    /**
     * 移除缓存
     * @param key 缓存键
     */
    fun removeCachedData(key: String) {
        cache.remove(key)
    }

    /**
     * 清除所有缓存
     */
    fun clearAllCache() {
        cache.clear()
    }

    /**
     * 清理过期缓存
     * @return 清理的缓存数量
     */
    fun cleanupExpiredCache(): Int {
        var count = 0
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val cachedData = entry.value
            if (!cachedData.isValid()) {
                iterator.remove()
                count++
            }
        }
        return count
    }

    /**
     * 获取缓存大小
     */
    fun getCacheSize(): Int {
        return cache.size
    }

    /**
     * 检查缓存是否存在且有效
     */
    fun containsValidCache(key: String): Boolean {
        val cached = cache[key]
        return cached != null && cached.isValid()
    }

    companion object {
        const val DEFAULT_EXPIRY_MILLIS = 60 * 60 * 1000L // 默认1小时过期
    }
}