package com.khm.group.center.utils.program

import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import java.io.InputStream
import java.util.Properties

/**
 * 版本号提取工具类
 * 负责从版本配置文件中提取和解析版本号
 */
object VersionExtractor {
    
    // 版本配置文件路径
    private const val VERSION_PROPERTIES_PATH = "settings/version.properties"
    private const val VERSION_KEY = "version"
    
    // 默认版本号
    private const val DEFAULT_VERSION = "1.0.0"
    
    /**
     * 获取当前程序版本号（包含SNAPSHOT等后缀）
     */
    fun getFullVersion(): String {
        return try {
            val properties = Properties()
            val inputStream: InputStream? = javaClass.classLoader.getResourceAsStream(VERSION_PROPERTIES_PATH)
            
            if (inputStream != null) {
                properties.load(inputStream)
                properties.getProperty(VERSION_KEY, DEFAULT_VERSION).trim()
            } else {
                logger.warn("Version properties file not found: $VERSION_PROPERTIES_PATH, using default version: $DEFAULT_VERSION")
                DEFAULT_VERSION
            }
        } catch (e: Exception) {
            logger.error("Failed to load version from properties file", e)
            DEFAULT_VERSION
        }
    }
    
    /**
     * 获取清理后的版本号（移除SNAPSHOT等后缀）
     * 格式：主版本号.次版本号.修订号
     */
    fun getCleanVersion(): String {
        val fullVersion = getFullVersion()
        return extractCleanVersion(fullVersion)
    }
    
    /**
     * 提取清理后的版本号
     * @param versionString 版本字符串（可能包含-SNAPSHOT等后缀）
     * @return 清理后的版本号
     */
    fun extractCleanVersion(versionString: String): String {
        return if (versionString.contains("-")) {
            versionString.substringBefore("-").trim()
        } else {
            versionString.trim()
        }
    }
    
    /**
     * 获取主版本号（第一个数字）
     */
    fun getMajorVersion(): Int {
        val cleanVersion = getCleanVersion()
        return extractMajorVersion(cleanVersion)
    }
    
    /**
     * 提取主版本号
     * @param versionString 版本字符串
     * @return 主版本号
     */
    fun extractMajorVersion(versionString: String): Int {
        return try {
            val parts = versionString.split(".")
            if (parts.isNotEmpty()) {
                parts[0].toInt()
            } else {
                1
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract major version from: $versionString, using default: 1", e)
            1
        }
    }
    
    /**
     * 获取次版本号（第二个数字）
     */
    fun getMinorVersion(): Int {
        val cleanVersion = getCleanVersion()
        return extractMinorVersion(cleanVersion)
    }
    
    /**
     * 提取次版本号
     * @param versionString 版本字符串
     * @return 次版本号
     */
    fun extractMinorVersion(versionString: String): Int {
        return try {
            val parts = versionString.split(".")
            if (parts.size >= 2) {
                parts[1].toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract minor version from: $versionString, using default: 0", e)
            0
        }
    }
    
    /**
     * 获取修订号（第三个数字）
     */
    fun getPatchVersion(): Int {
        val cleanVersion = getCleanVersion()
        return extractPatchVersion(cleanVersion)
    }
    
    /**
     * 提取修订号
     * @param versionString 版本字符串
     * @return 修订号
     */
    fun extractPatchVersion(versionString: String): Int {
        return try {
            val parts = versionString.split(".")
            if (parts.size >= 3) {
                parts[2].toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract patch version from: $versionString, using default: 0", e)
            0
        }
    }
    
    /**
     * 检查版本兼容性
     * @param cachedVersion 缓存的版本号
     * @param currentVersion 当前版本号
     * @return 是否需要清理缓存（主版本号变化时需要清理）
     */
    fun checkVersionCompatibility(cachedVersion: String, currentVersion: String): Boolean {
        val cachedMajor = extractMajorVersion(cachedVersion)
        val currentMajor = extractMajorVersion(currentVersion)
        
        return cachedMajor != currentMajor
    }
    
    /**
     * 获取版本信息字符串（用于日志输出）
     */
    fun getVersionInfo(): String {
        val fullVersion = getFullVersion()
        val cleanVersion = getCleanVersion()
        val major = getMajorVersion()
        val minor = getMinorVersion()
        val patch = getPatchVersion()
        
        return "Version: $fullVersion (Clean: $cleanVersion, Major: $major, Minor: $minor, Patch: $patch)"
    }
}