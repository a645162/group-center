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
    suspend fun checkProxyHealth(proxy: ProxyTestServer): Pair<Boolean, List<UrlTestResult>> {
        return withContext(Dispatchers.IO) {
            val enabledTestUrls = proxy.testConfig.getEnabledTestUrls()
            
            if (enabledTestUrls.isEmpty()) {
                logger.warn("Proxy ${proxy.nameEng} has no enabled test URLs, skipping health check")
                updateProxyStatus(proxy, false, null, "No enabled test URLs configured")
                return@withContext Pair(false, emptyList())
            }
            
            logger.debug("Starting proxy health check: ${proxy.nameEng}, type=${proxy.type}, address=${proxy.host}:${proxy.port}, testURLs=${enabledTestUrls.size}")
            
            // 测试所有启用的URL，只要有一个成功就认为代理可用
            var overallSuccess = false
            var bestResponseTime: Long? = null
            var lastError: String? = null
            val urlTestResults = mutableListOf<UrlTestResult>()
            
            for (testUrlConfig in enabledTestUrls) {
                val testStartTime = System.currentTimeMillis()
                var isSuccess = false
                var responseTime: Long? = null
                var statusCode: Int? = null
                var error: String? = null
                
                try {
                    val startTime = System.currentTimeMillis()
                    
                    // 创建代理配置的OkHttpClient
                    val okHttpClient = createOkHttpClientWithProxy(proxy)
                    
                    logger.debug("Testing proxy ${proxy.nameEng} with URL: ${testUrlConfig.url} (${testUrlConfig.nameEng})")
                    
                    val request = Request.Builder()
                        .url(testUrlConfig.url)
                        .build()
                    
                    val response = okHttpClient.newCall(request).execute()
                    val endTime = System.currentTimeMillis()
                    responseTime = endTime - startTime
                    
                    // 检查响应状态
                    statusCode = response.code
                    val expectedStatus = testUrlConfig.expectedStatusCode
                    isSuccess = statusCode == expectedStatus
                    
                    try {
                        if (isSuccess) {
                            logger.debug("Proxy ${proxy.nameEng} URL test successful: ${testUrlConfig.nameEng}, response time: ${responseTime}ms")
                            overallSuccess = true
                            if (bestResponseTime == null || responseTime < bestResponseTime) {
                                bestResponseTime = responseTime
                            }
                        } else {
                            logger.warn("Proxy ${proxy.nameEng} URL test failed: ${testUrlConfig.nameEng}, status code: $statusCode (expected: $expectedStatus)")
                            error = "HTTP status code: $statusCode (expected: $expectedStatus)"
                            lastError = "URL ${testUrlConfig.nameEng}: $error"
                        }
                    } finally {
                        // 确保响应体被正确关闭，避免连接泄漏
                        response.close()
                    }
                } catch (e: Exception) {
                    logger.error("Proxy ${proxy.nameEng} URL test exception: ${testUrlConfig.nameEng}, error: ${e.message}")
                    logger.debug("Exception type: ${e.javaClass.simpleName}")
                    
                    // 针对SSL/TLS错误的特殊处理
                    if (e.message?.contains("SSL") == true || e.message?.contains("TLS") == true) {
                        logger.warn("SSL/TLS handshake failed for URL ${testUrlConfig.nameEng}, proxy server may not support HTTPS CONNECT tunnel")
                    }
                    
                    error = e.message ?: "Unknown error"
                    lastError = "URL ${testUrlConfig.nameEng}: $error"
                }
                
                // 记录每个URL的测试结果
                urlTestResults.add(UrlTestResult(
                    url = testUrlConfig.url,
                    name = testUrlConfig.name,
                    nameEng = testUrlConfig.nameEng,
                    isSuccess = isSuccess,
                    responseTime = responseTime,
                    statusCode = statusCode,
                    error = error,
                    testTime = testStartTime
                ))
                
                // 注释掉提前结束的逻辑，确保测试所有URL
                // if (overallSuccess) {
                //     logger.debug("Proxy ${proxy.nameEng} health check successful, best response time: ${bestResponseTime}ms")
                //     break
                // }
            }
            
            if (overallSuccess) {
                logger.debug("Proxy ${proxy.nameEng} health check successful, best response time: ${bestResponseTime}ms")
                updateProxyStatus(proxy, true, bestResponseTime, null, urlTestResults)
                Pair(true, urlTestResults)
            } else {
                logger.warn("Proxy ${proxy.nameEng} health check failed for all test URLs")
                updateProxyStatus(proxy, false, null, lastError ?: "All URL tests failed", urlTestResults)
                Pair(false, urlTestResults)
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
    private fun updateProxyStatus(proxy: ProxyTestServer, isAvailable: Boolean, responseTime: Long?, error: String?, urlTestResults: List<UrlTestResult> = emptyList()) {
        val status = ProxyConfigManager.proxyStatusMap.getOrPut(proxy.nameEng) { ProxyStatus() }
        
        if (isAvailable) {
            status.updateSuccess(responseTime ?: 0)
        } else {
            status.updateFailure(error ?: "Unknown error")
        }
        
        // 存储URL测试结果
        status.lastUrlTestResults = urlTestResults
    }

    /**
     * 批量检查所有启用的代理服务器
     */
    suspend fun checkAllEnabledProxyTests(): Map<String, Pair<Boolean, List<UrlTestResult>>> {
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
            val successCount = results.count { it.second.first }
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
            totalChecks = status.successCount + status.failureCount,
            urlTestResults = status.lastUrlTestResults
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
                    totalChecks = status.successCount + status.failureCount,
                    urlTestResults = status.lastUrlTestResults
                )
            } else {
                null
            }
        }
    }
}

/**
 * 单个URL测试结果
 */
data class UrlTestResult(
    val url: String,
    val name: String,
    val nameEng: String,
    val isSuccess: Boolean,
    val responseTime: Long?,
    val statusCode: Int?,
    val error: String?,
    val testTime: Long
)

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
    val totalChecks: Int,
    val urlTestResults: List<UrlTestResult> = emptyList()
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