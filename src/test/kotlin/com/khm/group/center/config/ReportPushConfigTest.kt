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
 * Report push configuration test class
 * Corresponds to the functionality of Scripts/test-report-push.kt
 */
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.yml"])
class ReportPushConfigTest {

    @Test
    fun `test all config file formats`() {
        println("=== Testing report push configuration ===")

        // Config file paths to test
        val configFiles = listOf(
            "Config/Bot/bot-groups.yaml",
            "Config/Bot/bot-groups-test.yaml",
            "Config/Bot/bot-groups-example.yaml"
        )

        val yamlMapper = ObjectMapper(YAMLFactory())

        configFiles.forEach { configPath ->
            println("\nTesting config file: $configPath")

            try {
                if (Files.exists(Paths.get(configPath))) {
                    val yamlContent = Files.readString(Paths.get(configPath))
                    val botConfig = yamlMapper.readValue(yamlContent, Map::class.java)

                    val groups = (botConfig["bot"] as? Map<*, *>)?.get("groups") as? List<*>
                    println("✓ Config file format is correct")
                    println("  Found ${groups?.size ?: 0} group configurations")

                    groups?.forEachIndexed { index, group ->
                        val groupMap = group as? Map<*, *>
                        println("  Group ${index + 1}: ${groupMap?.get("name")} (Type: ${groupMap?.get("type")})")
                    }

                    // Validate config format
                    assertNotNull(botConfig["bot"], "Config file should contain bot configuration")
                } else {
                    println("✗ Config file does not exist")
                }
            } catch (e: Exception) {
                println("✗ Config file parsing error: ${e.message}")
                throw e // Rethrow exception to fail the test
            }
        }

        // Validate main config file exists
        val mainConfigFile = Paths.get("Config/Bot/bot-groups.yaml")
        assertTrue(Files.exists(mainConfigFile), "Main config file Config/Bot/bot-groups.yaml should exist")
    }

    @Test
    fun `test main config file parseability`() {
        val yamlMapper = ObjectMapper(YAMLFactory())
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")

        assertTrue(Files.exists(configFile), "Main config file should exist")

        val yamlContent = Files.readString(configFile)
        val botConfig = yamlMapper.readValue(yamlContent, Map::class.java)

        // Validate basic structure
        assertNotNull(botConfig["bot"], "Config file should contain bot configuration")
        val botSection = botConfig["bot"] as Map<*, *>
        assertNotNull(botSection["groups"], "Bot configuration should contain groups")

        val groups = botSection["groups"] as? List<*>
        assertNotNull(groups, "Groups should be a list")

        println("✅ Main config file parsed successfully, contains ${groups!!.size} group configurations")
    }

    @Test
    fun `test example config file existence`() {
        val exampleConfigFile = Paths.get("Config/Bot/bot-groups-example.yaml")
        if (Files.exists(exampleConfigFile)) {
            println("✓ Example config file exists")
            val yamlContent = Files.readString(exampleConfigFile)
            assertTrue(yamlContent.contains("groups:"), "Example config file should contain groups configuration")
        } else {
            println("⚠️ Example config file does not exist, but this is not an error")
        }
    }
}