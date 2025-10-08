package com.khm.group.center.controller.api.proxy

import com.khm.group.center.config.ProxyConfigLoader
import com.khm.group.center.datatype.config.ProxyConfigManager
import com.khm.group.center.datatype.config.ProxyTestServer
import com.khm.group.center.service.ProxyHealthCheckService
import com.khm.group.center.task.ProxyHealthCheckScheduler
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 代理服务器查询接口控制器
 * 提供代理服务器状态查询和管理功能
 */
@RestController
@RequestMapping("/api/proxy")
@Tag(name = "Proxy Management", description = "Proxy server status query and management API")
@Slf4jKt
class ProxyController {

    @Autowired
    private lateinit var proxyHealthCheckService: ProxyHealthCheckService

    @Autowired
    private lateinit var proxyHealthCheckScheduler: ProxyHealthCheckScheduler

    @Autowired
    private lateinit var proxyConfigLoader: ProxyConfigLoader

    /**
     * 获取所有代理服务器状态
     */
    @Operation(
        summary = "Get All Proxy Servers",
        description = "Retrieve status and details for all configured proxy servers"
    )
    @GetMapping("/servers")
    fun getAllProxyServers(): ResponseEntity<ProxyServersResponse> {
        if (!ProxyConfigManager.proxyConfig.enable) {
            return ResponseEntity.ok(ProxyServersResponse(
                success = false,
                message = "代理测试配置未启用",
                servers = emptyList(),
                totalCount = 0,
                availableCount = 0
            ))
        }

        val statusDetails = proxyHealthCheckService.getAllProxyTestStatusDetails()
        val availableCount = statusDetails.count { it.isAvailable }

        return ResponseEntity.ok(ProxyServersResponse(
            success = true,
            message = "获取代理测试服务器列表成功",
            servers = statusDetails.map { ProxyServerInfo.fromStatusDetails(it) },
            totalCount = statusDetails.size,
            availableCount = availableCount
        ))
    }

    /**
     * 获取指定代理服务器状态
     */
    @Operation(
        summary = "Get Specific Proxy Server",
        description = "Retrieve status and details for a specific proxy server by name"
    )
    @GetMapping("/servers/{nameEng}")
    fun getProxyServer(
        @Parameter(description = "English name of the proxy server")
        @PathVariable nameEng: String
    ): ResponseEntity<ProxyServerResponse> {
        if (!ProxyConfigManager.proxyConfig.enable) {
            return ResponseEntity.ok(ProxyServerResponse(
                success = false,
                message = "代理测试配置未启用",
                server = null
            ))
        }

        val statusDetail = proxyHealthCheckService.getProxyTestStatusDetails(nameEng)
        if (statusDetail == null) {
            return ResponseEntity.ok(ProxyServerResponse(
                success = false,
                message = "代理测试服务器不存在或未启用",
                server = null
            ))
        }

        return ResponseEntity.ok(ProxyServerResponse(
            success = true,
            message = "获取代理测试服务器信息成功",
            server = ProxyServerInfo.fromStatusDetails(statusDetail)
        ))
    }

    /**
     * 获取代理服务器总体状态
     */
    @Operation(
        summary = "Get Proxy Status Summary",
        description = "Retrieve overall proxy system status including availability rates and configuration status"
    )
    @GetMapping("/status")
    fun getProxyStatus(): ResponseEntity<ProxyStatusResponse> {
        val statusSummary = proxyHealthCheckScheduler.getStatusSummary()

        return ResponseEntity.ok(ProxyStatusResponse(
            success = true,
            message = "Get proxy status successfully",
            status = ProxyStatusInfo.fromSummary(statusSummary),
            configEnabled = ProxyConfigManager.proxyConfig.enable,
            configFileExists = proxyConfigLoader.configFileExists()
        ))
    }

    /**
     * 手动触发健康检查
     */
    @Operation(
        summary = "Trigger Manual Health Check",
        description = "Manually trigger health check for all proxy servers and return immediate results"
    )
    @PostMapping("/health-check")
    fun triggerHealthCheck(): ResponseEntity<HealthCheckResponse> {
        if (!ProxyConfigManager.proxyConfig.enable) {
            return ResponseEntity.ok(HealthCheckResponse(
                success = false,
                message = "代理测试配置未启用，无法执行健康检查"
            ))
        }

        return try {
            val results = runBlocking {
                proxyHealthCheckScheduler.triggerManualHealthCheck()
            }

            val successCount = results.count { it.value }
            val failedCount = results.size - successCount

            ResponseEntity.ok(HealthCheckResponse(
                success = true,
                message = "代理测试健康检查完成: 成功 $successCount 个, 失败 $failedCount 个",
                results = results
            ))
        } catch (e: Exception) {
            logger.error("Manual proxy health check failed: ${e.message}", e)
            ResponseEntity.ok(HealthCheckResponse(
                success = false,
                message = "代理测试健康检查执行失败: ${e.message}"
            ))
        }
    }

    /**
     * 重新加载代理配置
     */
    @Operation(
        summary = "Reload Proxy Configuration",
        description = "Reload proxy configuration from file without restarting the application"
    )
    @PostMapping("/reload-config")
    fun reloadConfig(): ResponseEntity<ReloadConfigResponse> {
        return try {
            val success = proxyConfigLoader.reloadConfig()
            if (success) {
                ResponseEntity.ok(ReloadConfigResponse(
                    success = true,
                    message = "代理配置重新加载成功"
                ))
            } else {
                ResponseEntity.ok(ReloadConfigResponse(
                    success = false,
                    message = "代理配置重新加载失败"
                ))
            }
        } catch (e: Exception) {
            logger.error("Reload configuration failed: ${e.message}", e)
            ResponseEntity.ok(ReloadConfigResponse(
                success = false,
                message = "重新加载配置失败: ${e.message}"
            ))
        }
    }
}

/**
 * 代理服务器信息响应
 */
data class ProxyServersResponse(
    val success: Boolean,
    val message: String,
    val servers: List<ProxyServerInfo>,
    val totalCount: Int,
    val availableCount: Int
)

/**
 * 单个代理服务器响应
 */
data class ProxyServerResponse(
    val success: Boolean,
    val message: String,
    val server: ProxyServerInfo?
)

/**
 * 代理状态响应
 */
data class ProxyStatusResponse(
    val success: Boolean,
    val message: String,
    val status: ProxyStatusInfo,
    val configEnabled: Boolean,
    val configFileExists: Boolean
)

/**
 * 健康检查响应
 */
data class HealthCheckResponse(
    val success: Boolean,
    val message: String,
    val results: Map<String, Boolean> = emptyMap()
)

/**
 * 重新加载配置响应
 */
data class ReloadConfigResponse(
    val success: Boolean,
    val message: String
)

/**
 * 代理服务器详细信息
 */
data class ProxyServerInfo(
    val name: String,
    val nameEng: String,
    val type: String,
    val host: String,
    val port: Int,
    val priority: Int,
    val enable: Boolean,
    val requiresAuth: Boolean,
    
    // 状态信息
    val isAvailable: Boolean,
    val lastCheckTime: Long?,
    val responseTime: Long?,
    val successRate: String,
    val totalChecks: Int,
    val lastError: String?,
    
    // 健康检查配置
    val healthCheckEnabled: Boolean,
    val healthCheckInterval: Int,
    val healthCheckTimeout: Int,
    val testUrls: List<String>
) {
    companion object {
        fun fromStatusDetails(details: com.khm.group.center.service.ProxyStatusDetails): ProxyServerInfo {
            return ProxyServerInfo(
                name = details.proxy.name,
                nameEng = details.proxy.nameEng,
                type = details.proxy.getTypeString(),
                host = details.proxy.host,
                port = details.proxy.port,
                priority = 0, // ProxyTestServer没有priority属性，使用默认值
                enable = details.proxy.enable,
                requiresAuth = details.proxy.requiresAuthentication(),
                
                isAvailable = details.isAvailable,
                lastCheckTime = details.lastCheckTime,
                responseTime = details.responseTime,
                successRate = "%.2f%%".format(details.successRate),
                totalChecks = details.totalChecks,
                lastError = details.status.lastError,
                
                healthCheckEnabled = details.proxy.testConfig.enable,
                healthCheckInterval = details.proxy.testConfig.interval,
                healthCheckTimeout = details.proxy.testConfig.timeout,
                testUrls = listOf(details.proxy.testConfig.testUrl, details.proxy.testConfig.directTestUrl)
            )
        }
    }
}

/**
 * 代理状态信息
 */
data class ProxyStatusInfo(
    val totalProxies: Int,
    val availableProxies: Int,
    val availabilityRate: String,
    val averageResponseTime: Long?,
    val lastCheckTime: Long,
    val isConfigEnabled: Boolean,
    val summaryDescription: String
) {
    companion object {
        fun fromSummary(summary: com.khm.group.center.task.ProxyStatusSummary): ProxyStatusInfo {
            return ProxyStatusInfo(
                totalProxies = summary.totalProxies,
                availableProxies = summary.availableProxies,
                availabilityRate = "%.2f%%".format(summary.availabilityRate),
                averageResponseTime = summary.averageResponseTime,
                lastCheckTime = summary.lastCheckTime,
                isConfigEnabled = summary.isConfigEnabled,
                summaryDescription = summary.getSummaryDescription()
            )
        }
    }
}