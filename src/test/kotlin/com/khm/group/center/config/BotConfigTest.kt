package com.khm.group.center.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Bot配置测试类
 * 对应 Scripts/test-group-pusher.kt 的功能
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class BotConfigTest {

    @Autowired
    private lateinit var botConfig: BotConfig

    @Test
    fun `测试Bot配置加载`() {
        assert(botConfig != null) { "Bot配置应该被正确加载" }
        assert(botConfig.bot != null) { "Bot配置中的bot字段不应该为空" }
        assert(botConfig.bot.groups != null) { "Bot配置中的groups字段不应该为空" }
    }

    @Test
    fun `测试配置文件存在性`() {
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        assertTrue(Files.exists(configFile), "配置文件 Config/Bot/bot-groups.yaml 应该存在")
    }

    @Test
    fun `测试群组配置统计`() {
        val alarmGroups = botConfig.bot.groups.filter { it.type == "alarm" && it.enable }
        val shortTermGroups = botConfig.bot.groups.filter { it.type == "shortterm" && it.enable }
        val longTermGroups = botConfig.bot.groups.filter { it.type == "longterm" && it.enable }

        println("📊 群组配置统计:")
        println("  报警群 (alarm): ${alarmGroups.size} 个启用群组")
        println("  短期群 (shortterm): ${shortTermGroups.size} 个启用群组")
        println("  长期群 (longterm): ${longTermGroups.size} 个启用群组")

        // 验证至少有一个群组配置
        assertTrue(botConfig.bot.groups.isNotEmpty(), "应该至少配置一个群组")
    }

    @Test
    fun `测试群组详情`() {
        println("\n🔍 群组详情:")
        botConfig.bot.groups.forEach { group ->
            if (group.enable) {
                println("  📋 ${group.name} (${group.type})")
                if (group.larkGroupBotId.isNotBlank() && group.larkGroupBotKey.isNotBlank()) {
                    println("     飞书机器人: 已配置")
                }
                if (group.weComGroupBotKey.isNotBlank()) {
                    println("     企业微信机器人: 已配置")
                }
            }
        }

        // 验证启用的群组都有基本配置
        val enabledGroups = botConfig.bot.groups.filter { it.enable }
        enabledGroups.forEach { group ->
            assertTrue(group.name.isNotBlank(), "启用的群组应该有名称")
            assertTrue(group.type.isNotBlank(), "启用的群组应该有类型")
        }
    }

    @Test
    fun `测试配置文件格式`() {
        // 验证配置文件可以被正确解析
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        val yamlContent = Files.readString(configFile)
        
        // 简单的格式验证
        assertTrue(yamlContent.contains("bot:"), "配置文件应该包含bot配置")
        assertTrue(yamlContent.contains("groups:"), "配置文件应该包含groups配置")
    }
}