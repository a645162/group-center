#!/usr/bin/env kotlin

@file:DependsOn("org.springframework.boot:spring-boot-starter-web:3.5.5")
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
@file:DependsOn("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 测试报告推送配置
 * 这个脚本用于验证Bot配置文件格式是否正确
 */

fun main() {
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
            } else {
                println("✗ 配置文件不存在")
            }
        } catch (e: Exception) {
            println("✗ 配置文件解析错误: ${e.message}")
        }
    }
    
    println("\n=== 测试完成 ===")
    println("请确保 Config/Bot/bot-groups.yaml 文件存在并包含正确的飞书bot配置")
    println("配置格式参考 Config/Bot/bot-groups-example.yaml")
}