#!/usr/bin/env kotlin

@file:DependsOn("org.springframework.boot:spring-boot-starter:3.5.5")
@file:DependsOn("com.fasterxml.jackson.core:jackson-databind:2.17.2")
@file:DependsOn("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.config.BotConfig
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 测试GroupPusher配置加载功能
 * 这个脚本用于验证Bot配置文件是否正确配置
 */
fun main() {
    println("🔍 测试GroupPusher配置加载...")
    
    try {
        // 测试配置文件是否存在
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        if (!Files.exists(configFile)) {
            println("❌ 配置文件不存在: ${configFile.toAbsolutePath()}")
            println("请创建 Config/Bot/bot-groups.yaml 文件")
            return
        }
        
        // 测试配置文件格式
        val yamlMapper = ObjectMapper(YAMLFactory())
        val yamlContent = Files.readString(configFile)
        val botConfig = yamlMapper.readValue(yamlContent, BotConfig::class.java)
        
        println("✅ 配置文件加载成功")
        
        // 检查各类型群组配置
        val alarmGroups = botConfig.bot.groups.filter { it.type == "alarm" && it.enable }
        val shortTermGroups = botConfig.bot.groups.filter { it.type == "shortterm" && it.enable }
        val longTermGroups = botConfig.bot.groups.filter { it.type == "longterm" && it.enable }
        
        println("📊 群组配置统计:")
        println("  报警群 (alarm): ${alarmGroups.size} 个启用群组")
        println("  短期群 (shortterm): ${shortTermGroups.size} 个启用群组")  
        println("  长期群 (longterm): ${longTermGroups.size} 个启用群组")
        
        // 显示各群组详情
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
        
        println("\n🎉 GroupPusher配置测试完成！")
        println("💡 提示: 在实际应用中可以通过以下方式使用:")
        println("   GroupPusher.pushToAlarmGroup(\"报警消息\")")
        println("   GroupPusher.pushToShortTermGroup(\"日报消息\")") 
        println("   GroupPusher.pushToLongTermGroup(\"月报消息\")")
        
    } catch (e: Exception) {
        println("❌ 配置加载失败: ${e.message}")
        println("请检查 Config/Bot/bot-groups.yaml 文件格式")
    }
}