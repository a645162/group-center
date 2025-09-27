package com.khm.group.center.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Bot configuration test class
 * Corresponds to the functionality of Scripts/test-group-pusher.kt
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class BotConfigTest {

    @Autowired
    private lateinit var botConfig: BotConfig

    @Test
    fun `test Bot config loading`() {
        assert(botConfig != null) { "Bot config should be loaded correctly" }
        assert(botConfig.bot != null) { "The 'bot' field in Bot config should not be null" }
        assert(botConfig.bot.groups != null) { "The 'groups' field in Bot config should not be null" }
    }

    @Test
    fun `test config file existence`() {
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        assertTrue(Files.exists(configFile), "Config file Config/Bot/bot-groups.yaml should exist")
    }

    @Test
    fun `test group config statistics`() {
        val alarmGroups = botConfig.bot.groups.filter { it.type == "alarm" && it.enable }
        val shortTermGroups = botConfig.bot.groups.filter { it.type == "shortterm" && it.enable }
        val longTermGroups = botConfig.bot.groups.filter { it.type == "longterm" && it.enable }

        println("📊 Group config statistics:")
        println("  Alarm groups (alarm): ${alarmGroups.size} enabled groups")
        println("  Short-term groups (shortterm): ${shortTermGroups.size} enabled groups")
        println("  Long-term groups (longterm): ${longTermGroups.size} enabled groups")

        // Verify at least one group config exists
        assertTrue(botConfig.bot.groups.isNotEmpty(), "At least one group should be configured")
    }

    @Test
    fun `test group details`() {
        println("\n🔍 Group details:")
        botConfig.bot.groups.forEach { group ->
            if (group.enable) {
                println("  📋 ${group.name} (${group.type})")
                if (group.larkGroupBotId.isNotBlank() && group.larkGroupBotKey.isNotBlank()) {
                    println("     Lark bot: Configured")
                }
                if (group.weComGroupBotKey.isNotBlank()) {
                    println("     WeCom bot: Configured")
                }
            }
        }

        // Verify enabled groups have basic config
        val enabledGroups = botConfig.bot.groups.filter { it.enable }
        enabledGroups.forEach { group ->
            assertTrue(group.name.isNotBlank(), "Enabled group should have a name")
            assertTrue(group.type.isNotBlank(), "Enabled group should have a type")
        }
    }

    @Test
    fun `test config file format`() {
        // Verify config file can be parsed correctly
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        val yamlContent = Files.readString(configFile)
        
        // Simple format validation
        assertTrue(yamlContent.contains("bot:"), "Config file should contain bot configuration")
        assertTrue(yamlContent.contains("groups:"), "Config file should contain groups configuration")
    }
}