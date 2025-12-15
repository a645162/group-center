package com.khm.group.center.service.cache

import com.alibaba.fastjson2.JSON
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.program.VersionExtractor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * 缓存版本管理器
 * 负责管理缓存目录的版本信息和兼容性检查
 */
object CacheVersionManager {
    
    // 当前程序版本（从配置文件读取）
    private val CURRENT_VERSION: String = VersionExtractor.getCleanVersion()
    
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
                logger.info("Create cache directory: $cacheDirPath")
                
                // 创建版本信息文件
                saveVersionInfo(cacheDirPath)
                return false // 新目录不需要清理
            }
            
            // 检查版本兼容性
            return checkVersionCompatibility(cacheDirPath)
        } catch (e: Exception) {
            logger.error("Failed to ensure cache directory: $cacheDirPath", e)
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
            logger.warn("Cache directory missing version info, may need cleanup: $cacheDirPath")
            saveVersionInfo(cacheDirPath) // 创建新的版本信息
            return true
        }
        
        // 使用版本提取工具检查兼容性
        val needCleanup = VersionExtractor.checkVersionCompatibility(versionInfo.version, CURRENT_VERSION)
        
        if (needCleanup) {
            logger.info("Program major version changed (${versionInfo.version} -> $CURRENT_VERSION), need cleanup cache: $cacheDirPath")
            saveVersionInfo(cacheDirPath)
            return true
        }
        
        logger.debug("Cache version compatibility check passed: $cacheDirPath (${versionInfo.version} -> $CURRENT_VERSION)")
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
            logger.error("Failed to save version info: $cacheDirPath", e)
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
            logger.error("Failed to load version info: $cacheDirPath", e)
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
                        logger.warn("Failed to delete cache file: $path", e)
                    }
                }
            
            // 清理完成后重新保存版本信息
            saveVersionInfo(cacheDirPath)
            logger.info("Cache directory cleanup completed: $cacheDirPath")
            true
        } catch (e: Exception) {
            logger.error("Failed to cleanup cache directory: $cacheDirPath", e)
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