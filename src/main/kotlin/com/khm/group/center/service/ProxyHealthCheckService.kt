package com.khm.group.center.service

import com.khm.group.center.datatype.config.ProxyConfigManager
import com.khm.group.center.datatype.config.ProxyTestServer
import com.khm.group.center.datatype.config.ProxyStatus
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.net.Proxy as JavaProxy
import java.util.concurrent.TimeUnit

/**
 * 代理服务器健康检查服务
 * 负责检查代理服务器的可用性和性能
 */
@Service
@Slf4jKt
class ProxyHealthCheckService {

    private val okHttpClientBuilder = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)

    /**
     * 检查单个代理服务器的健康状况
     */
    suspend fun checkProxyHealth(proxy: ProxyTestServer): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // 创建代理配置的OkHttpClient
                val okHttpClient = createOkHttpClientWithProxy(proxy)
                
                // 测试URL
                val testUrl = proxy.testConfig.testUrl
                
                logger.debug("Starting proxy health check: ${proxy.nameEng}, type=${proxy.type}, address=${proxy.host}:${proxy.port}, testURL=$testUrl")
                
                val request = Request.Builder()
                    .url(testUrl)
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                val endTime = System.currentTimeMillis()
                val responseTime = endTime - startTime
                
                // 检查响应状态
                val statusCode = response.code
                val isSuccess = statusCode in 200..399
                
                try {
                    if (isSuccess) {
                        logger.debug("Proxy ${proxy.nameEng} health check successful, response time: ${responseTime}ms")
                        updateProxyStatus(proxy, true, responseTime, null)
                        true
                    } else {
                        logger.warn("Proxy ${proxy.nameEng} health check failed, status code: $statusCode")
                        logger.debug("Proxy test details: HTTP proxy accessing HTTPS website, status code=$statusCode")
                        updateProxyStatus(proxy, false, null, "HTTP status code: $statusCode")
                        false
                    }
                } finally {
                    // 确保响应体被正确关闭，避免连接泄漏
                    response.close()
                }
            } catch (e: Exception) {
                logger.error("Proxy ${proxy.nameEng} health check exception: ${e.message}")
                logger.debug("Proxy details: type=${proxy.type}, address=${proxy.host}:${proxy.port}, testURL=${proxy.testConfig.testUrl}")
                logger.debug("Exception type: ${e.javaClass.simpleName}")
                
                // 针对SSL/TLS错误的特殊处理
                if (e.message?.contains("SSL") == true || e.message?.contains("TLS") == true) {
                    logger.warn("SSL/TLS handshake failed, proxy server may not support HTTPS CONNECT tunnel")
                }
                
                updateProxyStatus(proxy, false, null, e.message ?: "Unknown error")
                false
            }
        }
    }

    /**
     * 创建配置了代理的OkHttpClient
     */
    private fun createOkHttpClientWithProxy(proxy: ProxyTestServer): OkHttpClient {
        val proxyType = when (proxy.type) {
            com.khm.group.center.datatype.config.ProxyType.HTTP -> JavaProxy.Type.HTTP
            com.khm.group.center.datatype.config.ProxyType.HTTPS -> JavaProxy.Type.HTTP  // HTTPS代理使用HTTP类型
            com.khm.group.center.datatype.config.ProxyType.SOCKS5 -> JavaProxy.Type.SOCKS
        }
        
        val proxyAddress = InetSocketAddress(proxy.host, proxy.port)
        val javaProxy = JavaProxy(proxyType, proxyAddress)
        
        val builder = OkHttpClient.Builder()
            .connectTimeout(proxy.testConfig.timeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(proxy.testConfig.timeout.toLong(), TimeUnit.SECONDS)
            .proxy(javaProxy)
        
        // 设置认证（如果需要）
        if (proxy.requiresAuthentication()) {
            logger.debug("Proxy ${proxy.nameEng} requires authentication, but OkHttp authentication is not implemented yet")
        }
        
        return builder.build()
    }

    /**
     * 更新代理服务器状态
     */
    private fun updateProxyStatus(proxy: ProxyTestServer, isAvailable: Boolean, responseTime: Long?, error: String?) {
        val status = ProxyConfigManager.proxyStatusMap.getOrPut(proxy.nameEng) { ProxyStatus() }
        
        if (isAvailable) {
            status.updateSuccess(responseTime ?: 0)
        } else {
            status.updateFailure(error ?: "Unknown error")
        }
    }

    /**
     * 批量检查所有启用的代理服务器
     */
    suspend fun checkAllEnabledProxyTests(): Map<String, Boolean> {
        val enabledProxyTests = ProxyConfigManager.getEnabledProxyTests()
        
        if (enabledProxyTests.isEmpty()) {
            logger.debug("No enabled proxy test servers to check")
            return emptyMap()
        }
        
        logger.info("Starting health check for ${enabledProxyTests.size} enabled proxy test servers")
        
        return runBlocking {
            val checkJobs = enabledProxyTests.map { proxy: ProxyTestServer ->
                async(Dispatchers.IO) {
                    val result = checkProxyHealth(proxy)
                    proxy.nameEng to result
                }
            }
            
            val results = checkJobs.awaitAll()
            val successCount = results.count { it.second }
            val failedCount = results.size - successCount
            
            logger.info("Proxy health check completed: $successCount successful, $failedCount failed")
            
            results.toMap()
        }
    }

    /**
     * 获取代理服务器的详细状态信息
     */
    fun getProxyTestStatusDetails(proxyNameEng: String): ProxyStatusDetails? {
        val proxy = ProxyConfigManager.getProxyTestByNameEng(proxyNameEng) ?: return null
        val status = ProxyConfigManager.proxyStatusMap[proxyNameEng] ?: return null
        
        return ProxyStatusDetails(
            proxy = proxy,
            status = status,
            isConfigEnabled = ProxyConfigManager.proxyConfig.enable,
            lastCheckTime = status.lastCheckTime,
            isAvailable = status.isAvailable,
            responseTime = status.responseTime,
            successRate = status.getSuccessRate(),
            totalChecks = status.successCount + status.failureCount
        )
    }

    /**
     * 获取所有代理服务器的详细状态信息
     */
    fun getAllProxyTestStatusDetails(): List<ProxyStatusDetails> {
        return ProxyConfigManager.getEnabledProxyTests().mapNotNull { proxy: ProxyTestServer ->
            val status = ProxyConfigManager.proxyStatusMap[proxy.nameEng]
            if (status != null) {
                ProxyStatusDetails(
                    proxy = proxy,
                    status = status,
                    isConfigEnabled = ProxyConfigManager.proxyConfig.enable,
                    lastCheckTime = status.lastCheckTime,
                    isAvailable = status.isAvailable,
                    responseTime = status.responseTime,
                    successRate = status.getSuccessRate(),
                    totalChecks = status.successCount + status.failureCount
                )
            } else {
                null
            }
        }
    }
}

/**
 * 代理状态详细信息
 */
data class ProxyStatusDetails(
    val proxy: ProxyTestServer,
    val status: ProxyStatus,
    val isConfigEnabled: Boolean,
    val lastCheckTime: Long?,
    val isAvailable: Boolean,
    val responseTime: Long?,
    val successRate: Double,
    val totalChecks: Int
) {
    /**
     * 获取可读的状态描述
     */
    fun getStatusDescription(): String {
        return when {
            !isConfigEnabled -> "Configuration disabled"
            !proxy.enable -> "Proxy disabled"
            isAvailable -> "Available"
            else -> "Unavailable"
        }
    }
    
    /**
     * 获取响应时间描述
     */
    fun getResponseTimeDescription(): String {
        return responseTime?.let { "${it}ms" } ?: "N/A"
    }
    
    /**
     * 获取成功率描述
     */
    fun getSuccessRateDescription(): String {
        return "%.2f%%".format(successRate)
    }
}