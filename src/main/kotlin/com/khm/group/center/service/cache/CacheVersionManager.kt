package com.khm.group.center.service.cache

import com.alibaba.fastjson2.JSON
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * 缓存版本管理器
 * 负责管理缓存目录的版本信息和兼容性检查
 */
object CacheVersionManager {
    
    // 当前程序版本（格式：1.a.b）
    private const val CURRENT_VERSION = "1.0.0"
    
    // 版本信息文件名
    private const val VERSION_INFO_FILE = "Info.json"
    
    /**
     * 版本信息数据结构
     */
    data class VersionInfo(
        val version: String,
        val createdTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 确保缓存目录存在并检查版本兼容性
     * @param cacheDirPath 缓存目录路径
     * @return 是否需要清理缓存（true表示需要清理）
     */
    fun ensureCacheDirectoryWithVersion(cacheDirPath: Path): Boolean {
        try {
            // 确保目录存在
            if (!Files.exists(cacheDirPath)) {
                Files.createDirectories(cacheDirPath)
                logger.info("创建缓存目录: $cacheDirPath")
                
                // 创建版本信息文件
                saveVersionInfo(cacheDirPath)
                return false // 新目录不需要清理
            }
            
            // 检查版本兼容性
            return checkVersionCompatibility(cacheDirPath)
        } catch (e: Exception) {
            logger.error("确保缓存目录失败: $cacheDirPath", e)
            return false
        }
    }
    
    /**
     * 检查版本兼容性
     * @param cacheDirPath 缓存目录路径
     * @return 是否需要清理缓存（true表示需要清理）
     */
    private fun checkVersionCompatibility(cacheDirPath: Path): Boolean {
        val versionInfo = loadVersionInfo(cacheDirPath)
        
        if (versionInfo == null) {
            // 没有版本信息文件，可能是旧版本缓存，需要清理
            logger.warn("缓存目录缺少版本信息，可能需要清理: $cacheDirPath")
            saveVersionInfo(cacheDirPath) // 创建新的版本信息
            return true
        }
        
        // 解析版本号（格式：1.a.b）
        val currentVersionParts = CURRENT_VERSION.split(".")
        val cachedVersionParts = versionInfo.version.split(".")
        
        if (currentVersionParts.size < 2 || cachedVersionParts.size < 2) {
            logger.warn("版本格式错误，需要清理缓存: $cacheDirPath")
            saveVersionInfo(cacheDirPath)
            return true
        }
        
        // 检查主版本号（a）是否发生变化
        val currentMajor = currentVersionParts.getOrNull(1)?.toIntOrNull() ?: 0
        val cachedMajor = cachedVersionParts.getOrNull(1)?.toIntOrNull() ?: 0
        
        if (currentMajor != cachedMajor) {
            logger.info("程序主版本发生变化（${versionInfo.version} -> $CURRENT_VERSION），需要清理缓存: $cacheDirPath")
            saveVersionInfo(cacheDirPath)
            return true
        }
        
        logger.debug("缓存版本兼容性检查通过: $cacheDirPath (${versionInfo.version} -> $CURRENT_VERSION)")
        return false
    }
    
    /**
     * 保存版本信息到缓存目录
     */
    private fun saveVersionInfo(cacheDirPath: Path) {
        try {
            val versionInfo = VersionInfo(CURRENT_VERSION)
            val versionFile = cacheDirPath.resolve(VERSION_INFO_FILE)
            val jsonContent = JSON.toJSONString(versionInfo)
            
            Files.writeString(
                versionFile,
                jsonContent,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            
            logger.debug("保存版本信息: $versionFile")
        } catch (e: Exception) {
            logger.error("保存版本信息失败: $cacheDirPath", e)
        }
    }
    
    /**
     * 从缓存目录加载版本信息
     */
    private fun loadVersionInfo(cacheDirPath: Path): VersionInfo? {
        return try {
            val versionFile = cacheDirPath.resolve(VERSION_INFO_FILE)
            if (!Files.exists(versionFile)) {
                return null
            }
            
            val jsonContent = Files.readString(versionFile)
            JSON.parseObject(jsonContent, VersionInfo::class.java)
        } catch (e: Exception) {
            logger.error("加载版本信息失败: $cacheDirPath", e)
            null
        }
    }
    
    /**
     * 清理缓存目录（删除所有文件）
     */
    fun cleanupCacheDirectory(cacheDirPath: Path): Boolean {
        return try {
            if (!Files.exists(cacheDirPath)) {
                return true
            }
            
            Files.walk(cacheDirPath)
                .filter { it != cacheDirPath } // 不删除根目录本身
                .sorted(Comparator.reverseOrder()) // 先删除子文件和子目录
                .forEach { path ->
                    try {
                        Files.deleteIfExists(path)
                    } catch (e: Exception) {
                        logger.warn("删除缓存文件失败: $path", e)
                    }
                }
            
            logger.info("清理缓存目录完成: $cacheDirPath")
            true
        } catch (e: Exception) {
            logger.error("清理缓存目录失败: $cacheDirPath", e)
            false
        }
    }
    
    /**
     * 获取当前程序版本
     */
    fun getCurrentVersion(): String {
        return CURRENT_VERSION
    }
}