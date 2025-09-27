package com.khm.group.center.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Paths

@Configuration
class LarkBotConfig {

    @Bean
    fun larkGroupBot(): LarkGroupBot {
        val yamlMapper = ObjectMapper(YAMLFactory())
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        
        return try {
            val yamlContent = Files.readString(configFile)
            val botConfig = yamlMapper.readValue(yamlContent, BotConfig::class.java)
            
            // 查找第一个启用的飞书群配置
            val larkGroup = botConfig.bot.groups
                .firstOrNull { it.enable && it.larkGroupBotId.isNotBlank() }
            
            if (larkGroup != null) {
                LarkGroupBot(larkGroup.larkGroupBotId, larkGroup.larkGroupBotKey)
            } else {
                // 如果没有找到配置，返回一个无效的实例
                LarkGroupBot("", "")
            }
        } catch (e: Exception) {
            // 如果读取配置失败，返回一个无效的实例
            println("Failed to load Lark bot config: ${e.message}")
            LarkGroupBot("", "")
        }
    }
}