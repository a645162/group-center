package com.khm.group.center.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.config.BotConfig
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class GroupPusher {

    companion object {
        private lateinit var instance: GroupPusher

        @JvmStatic
        fun pushToAlarmGroup(message: String) {
            instance.pushToGroupsInternal(message, "alarm")
        }

        @JvmStatic
        fun pushToShortTermGroup(message: String) {
            instance.pushToGroupsInternal(message, "shortterm")
        }

        @JvmStatic
        fun pushToLongTermGroup(message: String) {
            instance.pushToGroupsInternal(message, "longterm")
        }

        @JvmStatic
        fun pushToGroup(message: String, groupType: String) {
            instance.pushToGroupsInternal(message, groupType)
        }
    }

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val yamlMapper = ObjectMapper(YAMLFactory())

    init {
        instance = this
    }

    /**
     * 推送到指定类型的群组（内部实现）
     */
    private fun pushToGroupsInternal(message: String, groupType: String) {
        try {
            val botConfig = loadBotConfig()
            val groups = botConfig.bot.groups.filter { it.type == groupType && it.enable }

            groups.forEach { group ->
                try {
                    // 推送到飞书群
                    if (group.larkGroupBotId.isNotBlank() && group.larkGroupBotKey.isNotBlank()) {
                        val larkBot = LarkGroupBot(group.larkGroupBotId, group.larkGroupBotKey)
                        if (larkBot.isValid()) {
                            larkBot.sendText(message)
                            println("✅ Successfully pushed to Lark ${groupType} group: ${group.name}")
                        }
                    }

                    // 推送到企业微信群
                    if (group.weComGroupBotKey.isNotBlank()) {
                        WeComGroupBot.directSendTextWithUrl(
                            group.weComGroupBotKey, message,
                            null, null
                        )
                        println("✅ Successfully pushed to WeCom ${groupType} group: ${group.name}")
                    }
                } catch (e: Exception) {
                    // 记录推送失败日志
                    println("❌ Failed to push to ${groupType} group ${group.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Failed to load bot config: ${e.message}")
        }
    }

    /**
     * 加载Bot配置
     */
    private fun loadBotConfig(): BotConfig {
        val configFile = Paths.get("Config/Bot/bot-groups.yaml")
        val yamlContent = Files.readString(configFile)
        return yamlMapper.readValue(yamlContent, BotConfig::class.java)
    }
}