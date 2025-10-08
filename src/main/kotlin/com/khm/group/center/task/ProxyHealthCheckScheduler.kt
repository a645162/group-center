package com.khm.group.center.task

import com.khm.group.center.config.ProxyConfigLoader
import com.khm.group.center.datatype.config.ProxyConfigManager
import com.khm.group.center.service.ProxyHealthCheckService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 代理服务器健康检查定时任务
 * 定期检查代理服务器的可用性和性能
 */
@Component
@Slf4jKt
class ProxyHealthCheckScheduler {

    @Autowired
    private lateinit var proxyHealthCheckService: ProxyHealthCheckService

    @Autowired
    private lateinit var proxyConfigLoader: ProxyConfigLoader

    private var isInitialized = false
    private var lastConfigModifiedTime: Long = -1L

    /**
     * 定期健康检查（每1小时执行一次）
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    fun scheduledHealthCheck() {
        if (!ProxyConfigManager.proxyConfig.enable) {
            logger.debug("Proxy test configuration not enabled, skipping health check")
            return
        }

        val enabledProxies = ProxyConfigManager.getEnabledProxyTests()
        if (enabledProxies.isEmpty()) {
            logger.debug("No enabled proxy test servers, skipping health check")
            return
        }

        logger.debug("Starting proxy test server health check, total ${enabledProxies.size} proxy test servers")

        // 使用协程并发执行健康检查
        runBlocking {
            try {
                val results = proxyHealthCheckService.checkAllEnabledProxyTests()
                val successCount = results.count { it.value }
                val failedCount = results.size - successCount

                logger.debug("Proxy health check completed: $successCount successful, $failedCount failed")

                // 记录详细的检查结果（仅记录失败的情况）
                results.forEach { (proxyName, isAvailable) ->
                    val status = ProxyConfigManager.proxyStatusMap[proxyName]
                    if (!isAvailable) {
                        logger.warn("Proxy test server $proxyName check failed, error: ${status?.lastError}")
                    }
                }
            } catch (e: Exception) {
                logger.error("Proxy health check execution exception: ${e.message}", e)
            }
        }
    }

    /**
     * 配置文件监控（每1分钟检查一次配置变更）
     */
    @Scheduled(fixedRate = 60000) // 1分钟 = 60000毫秒
    fun monitorConfigChanges() {
        if (!proxyConfigLoader.configFileExists()) {
            return
        }

        val currentModifiedTime = proxyConfigLoader.getConfigFileLastModified()
        if (currentModifiedTime > lastConfigModifiedTime) {
            logger.info("Detected proxy test configuration file change, reloading configuration")
            if (proxyConfigLoader.reloadConfig()) {
                lastConfigModifiedTime = currentModifiedTime
                logger.info("Proxy test configuration reloaded successfully")
            } else {
                logger.error("Proxy test configuration reload failed")
            }
        }
    }

    /**
     * 应用启动时初始化（启动后10秒执行）
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    fun initializeOnStartup() {
        if (isInitialized) {
            return
        }

        if (proxyConfigLoader.configFileExists()) {
            // 初始化代理测试状态
            ProxyConfigManager.initializeProxyStatus()
            
            // 记录初始配置修改时间
            lastConfigModifiedTime = proxyConfigLoader.getConfigFileLastModified()
            
            logger.info("Proxy health check service initialization completed")
            isInitialized = true
            
            // 立即执行一次健康检查
            runBlocking {
                try {
                    proxyHealthCheckService.checkAllEnabledProxyTests()
                } catch (e: Exception) {
                    logger.error("Initial proxy health check failed: ${e.message}", e)
                }
            }
        } else {
            logger.info("Proxy test configuration file does not exist, skipping initialization")
            isInitialized = true
        }
    }

    /**
     * 状态统计报告（每6小时执行一次）
     */
    @Scheduled(fixedRate = 21600000) // 6小时 = 21600000毫秒
    fun generateStatusReport() {
        if (!ProxyConfigManager.proxyConfig.enable) {
            return
        }

        val statusDetails = proxyHealthCheckService.getAllProxyTestStatusDetails()
        if (statusDetails.isEmpty()) {
            return
        }

        val availableCount = statusDetails.count { it.isAvailable }
        val totalCount = statusDetails.size
        val availabilityRate = if (totalCount > 0) availableCount.toDouble() / totalCount * 100 else 0.0

        logger.info("Proxy test server status statistics:")
        logger.info("Total proxy tests: $totalCount, available: $availableCount, availability rate: ${"%.2f".format(availabilityRate)}%")

        // 只记录失败的代理服务器详细信息
        statusDetails.filter { !it.isAvailable }.forEach { detail ->
            logger.warn("Proxy test ${detail.proxy.nameEng}: ${detail.getStatusDescription()}, " +
                       "response time: ${detail.getResponseTimeDescription()}, " +
                       "success rate: ${detail.getSuccessRateDescription()}")
        }
    }

    /**
     * 手动触发健康检查（用于测试或管理）
     */
    suspend fun triggerManualHealthCheck(): Map<String, Boolean> {
        logger.info("Manually triggering proxy health check")
        return proxyHealthCheckService.checkAllEnabledProxyTests()
    }

    /**
     * 获取当前代理状态摘要
     */
    fun getStatusSummary(): ProxyStatusSummary {
        val statusDetails = proxyHealthCheckService.getAllProxyTestStatusDetails()
        val availableCount = statusDetails.count { it.isAvailable }
        val totalCount = statusDetails.size
        val availabilityRate = if (totalCount > 0) availableCount.toDouble() / totalCount * 100 else 0.0

        val averageResponseTime = statusDetails
            .filter { it.responseTime != null }
            .mapNotNull { it.responseTime }
            .average()
            .let { if (it.isNaN()) null else it.toLong() }

        return ProxyStatusSummary(
            totalProxies = totalCount,
            availableProxies = availableCount,
            availabilityRate = availabilityRate,
            averageResponseTime = averageResponseTime,
            lastCheckTime = System.currentTimeMillis(),
            isConfigEnabled = ProxyConfigManager.proxyConfig.enable
        )
    }
}

/**
 * 代理状态摘要信息
 */
data class ProxyStatusSummary(
    val totalProxies: Int,
    val availableProxies: Int,
    val availabilityRate: Double,
    val averageResponseTime: Long?,
    val lastCheckTime: Long,
    val isConfigEnabled: Boolean
) {
    /**
     * 获取可读的摘要描述
     */
    fun getSummaryDescription(): String {
        return if (isConfigEnabled) {
            "Proxy status: $availableProxies/$totalProxies available (${"%.2f".format(availabilityRate)}%)" +
            (averageResponseTime?.let { ", average response time: ${it}ms" } ?: "")
        } else {
            "Proxy configuration disabled"
        }
    }
}