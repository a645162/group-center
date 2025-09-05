package com.khm.group.center.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 报告推送配置测试类
 * 对应 Scripts/test-report-push.kt 的功能
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class ReportPushConfigTest {

    @Test
    fun `测试所有配置文件格式`() {
        println("=== 测试报告推送配置 ===")

        // 测试配置文件路径
        val configFiles = listOf(
            "Config/Bot/bot-groups.yaml",
            "Config/Bot/bot-groups-test.yaml",
            "Config/Bot/bot-groups-example.yaml"
        )

        val yamlMapper = ObjectMapper(YAMLFactory())

        configFiles.forEach { configPath ->
            println("\n测试配置文件: $configPath")

            try {
                if (Files.exists(Paths.get(configPath))) {
                    val yamlContent = Files.readString(Paths.get(configPath))
                    val botConfig = yamlMapper.readValue(yamlContent, Map::class.java)

                    val groups = (botConfig["bot"] as? Map<*, *>)?.get("groups") as? List<*>
                    println("✓ 配置文件格式正确")
                    println("  找到 ${groups?.size ?: 0} 个群组配置")

                    groups?.forEachIndexed { index, group ->
                        val groupMap = group as? Map<*, *>
                        println("  群组 ${index + 1}: ${groupMap?.get("name")} (类型: ${groupMap?.get("type")})")
                    }

                    // 验证配置格式
                    assertNotNull(botConfig["bot"], "配置文件应该包含bot配置")
                } else {
                    println("✗ 配置文件不存在")
                }
            } catch (e: Exception) {
                println("✗ 配置文件解析错误: ${e.message}")
                throw e // 重新抛出异常使测试失败
            }
        }

        // 验证主配置文件存在
        val mainConfigFile = Paths.get("Config/Bot/bot-groups.yaml")
        assertTrue(Files.exists(mainConfigFile), "主配置文件 Config/Bot/bot-groups.yaml 应该存在")
    }

    @Test
    fun `测试主配置文件可解析性`() {
        val yamlMapper = ObjectMapper(YAMLFactory())
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")

        assertTrue(Files.exists(configFile), "主配置文件应该存在")

        val yamlContent = Files.readString(configFile)
        val botConfig = yamlMapper.readValue(yamlContent, Map::class.java)

        // 验证基本结构
        assertNotNull(botConfig["bot"], "配置文件应该包含bot配置")
        val botSection = botConfig["bot"] as Map<*, *>
        assertNotNull(botSection["groups"], "bot配置应该包含groups")

        val groups = botSection["groups"] as? List<*>
        assertNotNull(groups, "groups应该是一个列表")

        println("✅ 主配置文件解析成功，包含 ${groups!!.size} 个群组配置")
    }

    @Test
    fun `测试示例配置文件存在性`() {
        val exampleConfigFile = Paths.get("Config/Bot/bot-groups-example.yaml")
        if (Files.exists(exampleConfigFile)) {
            println("✓ 示例配置文件存在")
            val yamlContent = Files.readString(exampleConfigFile)
            assertTrue(yamlContent.contains("groups:"), "示例配置文件应该包含groups配置")
        } else {
            println("⚠️ 示例配置文件不存在，但这不是错误")
        }
    }
}