package com.khm.group.center.datatype.config

import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger

/**
 * 代理服务器测试配置主类
 */
data class ProxyConfig(
    val version: Int = 200,
    val enable: Boolean = false,
    val proxyTestList: List<ProxyTestServer> = emptyList(),
    val globalTestConfig: GlobalTestConfig = GlobalTestConfig()
)

/**
 * 单个代理服务器测试配置
 */
data class ProxyTestServer(
    val name: String = "",
    val nameEng: String = "",
    val type: ProxyType = ProxyType.HTTPS,
    val host: String = "",
    val port: Int = 8080,
    val username: String = "",
    val password: String = "",
    val enable: Boolean = false,  // 默认禁用，需要手动启用
    val testConfig: TestConfig = TestConfig()
) {
    /**
     * 获取代理服务器地址（host:port格式）
     */
    fun getProxyAddress(): String {
        return "$host:$port"
    }

    /**
     * 检查是否需要认证
     */
    fun requiresAuthentication(): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }

    /**
     * 获取代理类型字符串表示
     */
    fun getTypeString(): String {
        return when (type) {
            ProxyType.HTTP -> "http"
            ProxyType.HTTPS -> "https"
            ProxyType.SOCKS5 -> "socks5"
        }
    }
}

/**
 * 测试配置
 */
data class TestConfig(
    val enable: Boolean = true,
    val interval: Int = 300,  // 默认5分钟
    val timeout: Int = 10,    // 默认10秒
    val testUrl: String = "https://www.google.com",  // 通过代理访问的测试URL
    val directTestUrl: String = "https://www.google.com"  // 直接访问的对比URL
)

/**
 * 全局测试配置
 */
data class GlobalTestConfig(
    val enableDirectTest: Boolean = true,  // 是否启用直接访问对比测试
    val retryCount: Int = 2,               // 测试失败重试次数
    val retryDelay: Int = 5                // 测试失败后的等待时间（秒）
)

/**
 * 代理类型枚举
 */
enum class ProxyType {
    HTTP, HTTPS, SOCKS5
}

/**
 * 代理服务器状态信息
 */
data class ProxyStatus(
    var lastCheckTime: Long? = null,      // 最后一次检查时间戳
    var isAvailable: Boolean = false,     // 当前是否可用
    var responseTime: Long? = null,       // 响应时间（毫秒）
    var lastError: String? = null,        // 最后一次错误信息
    var successCount: Int = 0,            // 成功次数
    var failureCount: Int = 0,            // 失败次数
    var lastSuccessTime: Long? = null     // 最后一次成功时间
) {
    /**
     * 获取成功率
     */
    fun getSuccessRate(): Double {
        val total = successCount + failureCount
        return if (total > 0) successCount.toDouble() / total * 100 else 0.0
    }

    /**
     * 更新成功状态
     */
    fun updateSuccess(responseTimeMs: Long) {
        lastCheckTime = System.currentTimeMillis()
        isAvailable = true
        this.responseTime = responseTimeMs
        lastError = null
        successCount++
        lastSuccessTime = lastCheckTime
    }

    /**
     * 更新失败状态
     */
    fun updateFailure(error: String) {
        lastCheckTime = System.currentTimeMillis()
        isAvailable = false
        responseTime = null
        lastError = error
        failureCount++
    }
}

/**
 * 代理配置伴生对象，用于全局访问
 */
object ProxyConfigManager {
    var proxyConfig: ProxyConfig = ProxyConfig()
    var proxyStatusMap: MutableMap<String, ProxyStatus> = mutableMapOf()

    /**
     * 获取所有启用的代理测试服务器
     */
    fun getEnabledProxyTests(): List<ProxyTestServer> {
        return proxyConfig.proxyTestList
            .filter { it.enable && it.testConfig.enable && proxyConfig.enable }
    }

    /**
     * 根据英文名称获取代理测试服务器
     */
    fun getProxyTestByNameEng(nameEng: String): ProxyTestServer? {
        return proxyConfig.proxyTestList.firstOrNull { it.nameEng == nameEng && it.enable }
    }

    /**
     * 获取可用的代理测试服务器
     */
    fun getAvailableProxyTests(): List<ProxyTestServer> {
        return getEnabledProxyTests().filter { proxy ->
            val status = proxyStatusMap[proxy.nameEng]
            status?.isAvailable == true
        }
    }

    /**
     * 初始化代理测试状态
     */
    fun initializeProxyStatus() {
        proxyConfig.proxyTestList.forEach { proxy ->
            if (proxy.enable && proxy.testConfig.enable) {
                proxyStatusMap[proxy.nameEng] = ProxyStatus()
            }
        }
        logger.info("Proxy test status initialization completed, total ${proxyStatusMap.size} proxy test servers")
    }
}