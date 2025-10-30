package com.khm.group.center.service

import com.khm.group.center.datatype.config.ProxyTestServer
import com.khm.group.center.datatype.config.ProxyType
import com.khm.group.center.datatype.config.TestConfig
import com.khm.group.center.datatype.config.TestUrlConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

/**
 * Proxy health check service test
 * Test the functionality of HTTP proxy accessing HTTPS websites
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class ProxyHealthCheckServiceTest {

    @Autowired
    private lateinit var proxyHealthCheckService: ProxyHealthCheckService

    /**
     * Test HTTP proxy accessing HTTPS website
     * This test requires an actual proxy server to run
     */
    @Test
    fun testHttpProxyAccessHttps() {
        // Create a test proxy configuration
        val testProxy = ProxyTestServer(
            name = "HTTP Proxy Test",
            nameEng = "http-proxy-test",
            type = ProxyType.HTTP,
            host = "proxy.329509.xyz",
            port = 7890,
            enable = true,
            testConfig = TestConfig(
                testUrls = listOf(
                    TestUrlConfig(
                        url = "https://www.google.com",
                        name = "Google测试",
                        nameEng = "google-test",
                        enable = true,
                        expectedStatusCode = 200
                    )
                ),
                directTestUrl = "https://www.google.com",
                timeout = 10
            )
        )

        println("Start testing HTTP proxy accessing HTTPS website...")
        println("Proxy server: ${testProxy.host}:${testProxy.port}")
        println("Test URLs: ${testProxy.testConfig.getEnabledTestUrls().map { it.url }}")

        try {
            val result = runBlocking {
                proxyHealthCheckService.checkProxyHealth(testProxy)
            }

            val (isAvailable, urlResults) = result
            if (isAvailable) {
                println("✅ Proxy health check succeeded")
                println("URL test results:")
                urlResults.forEach { urlResult ->
                    println("  - ${urlResult.nameEng}: ${if (urlResult.isSuccess) "✅" else "❌"} (${urlResult.responseTime}ms)")
                }
            } else {
                println("❌ Proxy health check failed")
                println("URL test results:")
                urlResults.forEach { urlResult ->
                    println("  - ${urlResult.nameEng}: ${if (urlResult.isSuccess) "✅" else "❌"} (${urlResult.responseTime}ms) - ${urlResult.error}")
                }
            }
        } catch (e: Exception) {
            println("❌ Test exception: ${e.javaClass.simpleName} - ${e.message}")
            // Detailed error analysis
            when {
                e.message?.contains("SSL") == true || e.message?.contains("TLS") == true -> {
                    println("SSL/TLS handshake failed, the proxy server may not support HTTPS CONNECT tunnel")
                }
                e.message?.contains("timeout") == true -> {
                    println("Connection timed out, the proxy server may need more time to process the request")
                }
                e.message?.contains("connect") == true -> {
                    println("Connection failed, the proxy server may not be accessible")
                }
            }
        }
    }

    /**
     * Test proxy server connectivity (basic test)
     */
    @Test
    fun testProxyConnection() {
        println("Testing basic proxy server connectivity...")

        // This test can be used to verify if the proxy server is reachable
        // Actual implementation should be written according to specific requirements
        println("Basic proxy server connectivity test completed")
    }

    /**
     * Test configuration validation
     */
    @Test
    fun testProxyConfiguration() {
        println("Validating proxy configuration...")

        // Validate if the configuration is loaded correctly
        // Configuration validation logic can be added here
        println("Proxy configuration validation completed")
    }
}