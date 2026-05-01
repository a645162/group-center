package com.khm.group.center.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.khm.group.center.datatype.config.ProxyConfig
import com.khm.group.center.datatype.config.ProxyConfigManager
import com.khm.group.center.datatype.config.ProxyTestServer
import com.khm.group.center.datatype.config.ProxyStatus
import com.khm.group.center.datatype.config.ProxyType
import com.khm.group.center.datatype.config.TestUrlConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 代理配置加载器
 * 负责加载和解析代理服务器配置文件
 */
@Configuration
class ProxyConfigLoader {

    private val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    @Value("\${proxy.config.file:Config/Proxy/proxy.yaml}")
    private lateinit var configFilePath: String

    /**
     * 加载代理配置并创建配置Bean
     */
    @Bean
    fun loadProxyConfig(): ProxyConfig {
        val configFile = resolveConfigFile()

        return try {
            if (configFile == null || !Files.exists(configFile)) {
                logger.warn("Proxy configuration file does not exist: ${configFile ?: configFilePath}, using default configuration")
                return ProxyConfig()
            }

            logger.info("Loading proxy configuration from: $configFile")
            val yamlContent = Files.readString(configFile)
            logger.debug("Raw YAML content: $yamlContent")
            
            val proxyConfig = yamlMapper.readValue(yamlContent, ProxyConfig::class.java)
            logger.debug("Parsed proxy config: version=${proxyConfig.version}, enable=${proxyConfig.enable}, proxyTestList size=${proxyConfig.proxyTestList.size}")
            
            // 验证和修复配置
            val validatedConfig = validateAndFixConfig(proxyConfig)
            
            // 更新全局配置管理器
            ProxyConfigManager.proxyConfig = validatedConfig

            logger.info("Proxy test configuration loaded successfully, version: ${validatedConfig.version}, enabled: ${validatedConfig.enable}, proxy test count: ${validatedConfig.proxyTestList.size}")

            // 控制台输出配置摘要
            val enabledList = validatedConfig.proxyTestList.filter { it.enable }
            println("Proxy Config(${enabledList.size}/${validatedConfig.proxyTestList.size} enabled):")
            if (validatedConfig.proxyTestList.isEmpty()) {
                println("  (no proxy servers configured)")
            } else {
                for (proxy in validatedConfig.proxyTestList) {
                    val status = if (proxy.enable) "enabled" else "disabled"
                    println("Proxy: ${proxy.name}")
                    println("  NameEng: ${proxy.nameEng}")
                    println("  Host: ${proxy.host}")
                    println("  Port: ${proxy.port}")
                    println("  Type: ${proxy.getTypeString()}")
                    println("  Status: $status")
                }
            }
            println()
            
            validatedConfig
        } catch (e: Exception) {
            logger.error("Failed to load proxy configuration file: ${e.message}", e)
            ProxyConfig()
        }
    }

    /**
     * 验证和修复配置数据
     */
    private fun validateAndFixConfig(config: ProxyConfig): ProxyConfig {
        val validatedProxyTestList = config.proxyTestList.map { proxy ->
            // 修复代理类型
            val fixedType = when (proxy.type) {
                ProxyType.HTTP -> ProxyType.HTTP
                ProxyType.HTTPS -> ProxyType.HTTPS
                ProxyType.SOCKS5 -> ProxyType.SOCKS5
            }

            // 验证端口范围
            val fixedPort = when {
                proxy.port <= 0 -> {
                    logger.warn("Proxy test ${proxy.nameEng} port invalid: ${proxy.port}, using default port 8080")
                    8080
                }
                proxy.port > 65535 -> {
                    logger.warn("Proxy test ${proxy.nameEng} port out of range: ${proxy.port}, using default port 8080")
                    8080
                }
                else -> proxy.port
            }

            // 验证测试配置
            val fixedTestUrls = if (proxy.testConfig.testUrls.isEmpty()) {
                // 向后兼容：如果没有配置testUrls，使用默认URL
                listOf(
                    TestUrlConfig(
                        url = "https://www.google.com",
                        name = "默认测试",
                        nameEng = "default-test",
                        enable = true,
                        expectedStatusCode = 200
                    )
                )
            } else {
                // 验证和修复测试URL配置
                proxy.testConfig.testUrls.map { testUrl ->
                    TestUrlConfig(
                        url = if (testUrl.url.isBlank()) {
                            logger.warn("Proxy test ${proxy.nameEng} test URL is empty, using default URL")
                            "https://www.google.com"
                        } else {
                            testUrl.url
                        },
                        name = if (testUrl.name.isBlank()) {
                            "未命名测试"
                        } else {
                            testUrl.name
                        },
                        nameEng = if (testUrl.nameEng.isBlank()) {
                            "unnamed-test"
                        } else {
                            testUrl.nameEng
                        },
                        enable = testUrl.enable,
                        expectedStatusCode = if (testUrl.expectedStatusCode <= 0) {
                            logger.warn("Proxy test ${proxy.nameEng} expected status code invalid: ${testUrl.expectedStatusCode}, using default 200")
                            200
                        } else {
                            testUrl.expectedStatusCode
                        }
                    )
                }
            }
            
            val fixedTestConfig = proxy.testConfig.copy(
                interval = if (proxy.testConfig.interval <= 0) {
                    logger.warn("Proxy test ${proxy.nameEng} check interval invalid: ${proxy.testConfig.interval}, using default 300 seconds")
                    300
                } else {
                    proxy.testConfig.interval
                },
                timeout = if (proxy.testConfig.timeout <= 0) {
                    logger.warn("Proxy test ${proxy.nameEng} timeout invalid: ${proxy.testConfig.timeout}, using default 10 seconds")
                    10
                } else {
                    proxy.testConfig.timeout
                },
                testUrls = fixedTestUrls,
                directTestUrl = if (proxy.testConfig.directTestUrl.isBlank()) {
                    logger.warn("Proxy test ${proxy.nameEng} direct test URL is empty, using default URL")
                    "https://www.google.com"
                } else {
                    proxy.testConfig.directTestUrl
                }
            )

            proxy.copy(
                type = fixedType,
                port = fixedPort,
                testConfig = fixedTestConfig
            )
        }

        return config.copy(proxyTestList = validatedProxyTestList)
    }

    /**
     * 重新加载配置（用于热更新）
     */
    fun reloadConfig(): Boolean {
        return try {
            val newConfig = loadProxyConfig()
            ProxyConfigManager.proxyConfig = newConfig
            ProxyConfigManager.initializeProxyStatus()
            logger.info("Proxy test configuration reloaded successfully")
            true
        } catch (e: Exception) {
            logger.error("Failed to reload proxy configuration: ${e.message}", e)
            false
        }
    }

    /**
     * 检查配置文件是否存在
     */
    fun configFileExists(): Boolean {
        val configFile = resolveConfigFile()
        return configFile != null && Files.exists(configFile)
    }

    /**
     * 获取配置文件的最后修改时间
     */
    fun getConfigFileLastModified(): Long {
        val configFile = resolveConfigFile()
        return try {
            if (configFile != null) Files.getLastModifiedTime(configFile).toMillis() else -1L
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * 解析配置文件路径，依次尝试：配置值、classpath、项目根目录
     */
    private fun resolveConfigFile(): java.nio.file.Path? {
        // 1. 直接使用配置的路径（绝对路径或相对路径）
        val directPath = Paths.get(configFilePath)
        if (Files.exists(directPath)) {
            return directPath
        }

        // 2. 尝试从classpath加载
        val classpathResource = javaClass.classLoader.getResource(configFilePath)
        if (classpathResource != null) {
            return try {
                java.nio.file.Paths.get(classpathResource.toURI())
            } catch (e: Exception) {
                null
            }
        }

        // 3. 尝试以user.dir为基准
        val userDir = System.getProperty("user.dir")
        if (userDir != null) {
            val userDirPath = Paths.get(userDir, configFilePath)
            if (Files.exists(userDirPath)) {
                return userDirPath
            }
        }

        return directPath  // 返回原始路径，让调用方处理不存在的情况
    }
}

/**
 * 代理配置服务
 * 提供代理配置的访问和管理功能
 */
@Service
class ProxyConfigService {

    @Autowired
    private lateinit var proxyConfigLoader: ProxyConfigLoader

    /**
     * 获取当前代理配置
     */
    fun getCurrentConfig(): ProxyConfig {
        return ProxyConfigManager.proxyConfig
    }

    /**
     * 获取所有启用的代理测试服务器
     */
    fun getEnabledProxyTests(): List<ProxyTestServer> {
        return ProxyConfigManager.getEnabledProxyTests()
    }

    /**
     * 根据名称获取代理测试服务器
     */
    fun getProxyTestByNameEng(nameEng: String): ProxyTestServer? {
        return ProxyConfigManager.getProxyTestByNameEng(nameEng)
    }

    /**
     * 获取可用的代理测试服务器
     */
    fun getAvailableProxyTests(): List<ProxyTestServer> {
        return ProxyConfigManager.getAvailableProxyTests()
    }

    /**
     * 重新加载配置
     */
    fun reloadConfig(): Boolean {
        return proxyConfigLoader.reloadConfig()
    }

    /**
     * 检查配置是否启用
     */
    fun isConfigEnabled(): Boolean {
        return ProxyConfigManager.proxyConfig.enable
    }

    /**
     * 获取代理服务器状态
     */
    fun getProxyStatus(nameEng: String): ProxyStatus? {
        return ProxyConfigManager.proxyStatusMap[nameEng]
    }

    /**
     * 获取所有代理服务器状态
     */
    fun getAllProxyStatus(): Map<String, ProxyStatus> {
        return ProxyConfigManager.proxyStatusMap.toMap()
    }
}