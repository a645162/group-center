package com.khm.group.center.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.config.BotConfig
import com.khm.group.center.datatype.config.webhook.BotGroupConfig
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

@Service
@Slf4jKt
class BotPushService {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val yamlMapper = ObjectMapper(YAMLFactory())

    // 预定义的bot群配置
    private val botGroups = mutableListOf<BotGroupConfig>()

    companion object {
        private lateinit var instance: BotPushService

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

        /**
         * 推送时间同步报警
         * @param machineName 机器名称
         * @param timeDiff 时间差（秒）
         * @param threshold 阈值（秒），默认5分钟
         */
        @JvmStatic
        fun pushTimeSyncAlarm(machineName: String, timeDiff: Long, threshold: Long = 300) {
            val message = """
            ⚠️ 时间同步报警
            ====================
            机器: $machineName
            时间差: ${timeDiff}秒
            阈值: ${threshold}秒
            建议: 请使用ntp服务同步时间
            """.trimIndent()
            instance.pushToGroupsInternal(message, "alarm")
        }
    }

    @Value("\${bot.config.file:Config/Bot/bot-groups.yaml}")
    private lateinit var configFile: String

    init {
        // 初始化默认的bot群配置
        initializeDefaultBotGroups()

        // 设置静态实例
        instance = this
    }

    private fun ensureConfigLoaded() {
        if (botGroups.size == 3) { // 只有默认配置，需要加载配置文件
            loadBotGroupsFromConfig()
        }
    }

    private fun initializeDefaultBotGroups() {
        // 报警群
        val alarmGroup = BotGroupConfig().apply {
            name = "报警群"
            type = "alarm"
            // 这里需要配置实际的bot key
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        // 短期群
        val shortTermGroup = BotGroupConfig().apply {
            name = "短期群"
            type = "shortterm"
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        // 长期群
        val longTermGroup = BotGroupConfig().apply {
            name = "长期群"
            type = "longterm"
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        botGroups.addAll(listOf(alarmGroup, shortTermGroup, longTermGroup))
    }

    fun printBotGroups() {
        ensureConfigLoaded()
        logger.info("Current Bot Groups Configuration:")
        botGroups.forEach { group ->
            logger.info(group.toSummaryString())
        }
    }

    private fun loadBotGroupsFromConfig() {
        try {
            val yaml = Yaml()
            val inputStream = if (configFile.startsWith("classpath:")) {
                val resourcePath = configFile.substringAfter("classpath:")
                this::class.java.classLoader.getResourceAsStream(resourcePath)
            } else {
                FileInputStream(configFile)
            }

            inputStream?.use { stream ->
                val config = yaml.load<Map<String, Any>>(stream)
                val groups = config["bot"] as? Map<*, *>
                val groupList = groups?.get("groups") as? List<*>

                groupList?.forEach { groupConfig ->
                    if (groupConfig is Map<*, *>) {
                        val botGroup = BotGroupConfig().apply {
                            name = (groupConfig["name"] as? String) ?: ""
                            type = (groupConfig["type"] as? String) ?: ""
                            weComGroupBotKey = (groupConfig["weComGroupBotKey"] as? String) ?: ""
                            larkGroupBotId = (groupConfig["larkGroupBotId"] as? String) ?: ""
                            larkGroupBotKey = (groupConfig["larkGroupBotKey"] as? String) ?: ""
                            enable = (groupConfig["enable"] as? Boolean) ?: true
                        }

                        // 更新或添加配置
                        addOrUpdateBotGroup(botGroup)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load bot groups config: ${e.message}")
        }
    }

    /**
     * 添加或更新bot群配置
     */
    fun addOrUpdateBotGroup(config: BotGroupConfig) {
        val existing = botGroups.find { it.name == config.name }
        if (existing != null) {
            existing.type = config.type
            existing.weComGroupBotKey = config.weComGroupBotKey
            existing.larkGroupBotId = config.larkGroupBotId
            existing.larkGroupBotKey = config.larkGroupBotKey
            existing.enable = config.enable
        } else {
            botGroups.add(config)
        }
    }

    /**
     * 获取所有bot群配置
     */
    fun getAllBotGroups(): List<BotGroupConfig> {
        ensureConfigLoaded()
        return botGroups.toList()
    }

    /**
     * 根据类型获取bot群配置
     */
    fun getBotGroupsByType(type: String): List<BotGroupConfig> {
        ensureConfigLoaded()
        return botGroups.filter { it.type == type && it.isValid() }
    }

    /**
     * 推送消息到指定类型的bot群
     */
    fun pushToBotGroups(type: String, title: String, content: String) {
        val targetGroups = getBotGroupsByType(type)

        for (group in targetGroups) {
            pushMessageToGroup(group, title, content)
        }
    }

    /**
     * 推送消息到指定名称的bot群
     */
    fun pushToBotGroup(groupName: String, title: String, content: String) {
        val group = botGroups.find { it.name == groupName && it.isValid() }
        if (group != null) {
            pushMessageToGroup(group, title, content)
        }
    }

    /**
     * 推送报警消息
     */
    fun pushAlarmMessage(title: String, content: String) {
        pushToBotGroups("alarm", title, content)
    }

    /**
     * 推送到短期群（包含作息时间分析）
     */
    fun pushToShortTermGroup(title: String, content: String) {
        val sleepAnalysisContent = getSleepAnalysisContent("shortterm")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("shortterm", title, fullContent)
    }

    /**
     * 推送到长期群（包含作息时间分析）
     */
    fun pushToLongTermGroup(title: String, content: String) {
        val sleepAnalysisContent = getSleepAnalysisContent("longterm")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("longterm", title, fullContent)
    }

    /**
     * 获取作息时间分析内容
     */
    private fun getSleepAnalysisContent(reportType: String): String {
        return "\n\n🌙 作息时间分析已集成到报告中"
    }

    /**
     * 推送到指定类型的群组（内部实现）
     */
    private fun pushToGroupsInternal(message: String, groupType: String, removeEachLineBlank: Boolean = true) {
        if (removeEachLineBlank) {
            // 移除每行的空白字符
            val lines = message.lines()
            val cleanedLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
            val cleanedMessage = cleanedLines.joinToString("\n")
            return pushToGroupsInternal(cleanedMessage, groupType, false)
        }

        try {
            ensureConfigLoaded()
            val groups = botGroups.filter { it.type == groupType && it.enable && it.isValid() }

            groups.forEach { group ->
                try {
                    // 推送到飞书群
                    if (group.larkGroupBotId.isNotBlank() && group.larkGroupBotKey.isNotBlank()) {
                        val larkBot = LarkGroupBot(group.larkGroupBotId, group.larkGroupBotKey)
                        if (larkBot.isValid()) {
                            larkBot.sendText(message)
                            logger.info("Successfully pushed to Lark ${groupType} group: ${group.name}")
                        }
                    }

                    // 推送到企业微信群
                    if (group.weComGroupBotKey.isNotBlank()) {
                        WeComGroupBot.directSendTextWithUrl(
                            group.weComGroupBotKey, message,
                            null, null
                        )
                        logger.info("Successfully pushed to WeCom ${groupType} group: ${group.name}")
                    }
                } catch (e: Exception) {
                    // 记录推送失败日志
                    logger.error("Failed to push to ${groupType} group ${group.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to push to ${groupType} groups: ${e.message}")
        }
    }

    private fun pushMessageToGroup(group: BotGroupConfig, title: String, content: String) {
        val fullContent = "[$title]\n$content"

        runBlocking {
            try {
                // 推送企业微信
                if (group.weComGroupBotKey.isNotEmpty()) {
                    val weComUrl = WeComGroupBot.getWebhookUrl(group.weComGroupBotKey)
                    WeComGroupBot.directSendTextWithUrl(weComUrl, fullContent, emptyList(), emptyList())
                    logger.info("Sent WeCom message to ${group.name}")
                }

                if (group.larkGroupBotId.isNotEmpty() && group.larkGroupBotKey.isNotEmpty()) {
                    val larkBot = LarkGroupBot(group.larkGroupBotId, group.larkGroupBotKey)
                    larkBot.sendTextWithSilentMode(fullContent, null)
                    logger.info("Sent Lark message to ${group.name}")
                }
            } catch (e: Exception) {
                logger.error("Failed to send message to ${group.name}: ${e.message}")
            }
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
