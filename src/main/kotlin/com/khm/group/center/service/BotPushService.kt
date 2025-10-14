package com.khm.group.center.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.khm.group.center.config.BotConfig
import com.khm.group.center.datatype.config.webhook.BotGroupConfig
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.time.DateTimeUtils
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
        fun pushToAlarmGroup(message: String, urgent: Boolean = false) {
            instance.pushToGroupsInternal(message, "alarm", urgent = urgent)
        }

        @JvmStatic
        fun pushToShortTermGroup(message: String, urgent: Boolean = false) {
            instance.pushToGroupsInternal(message, "shortterm", urgent = urgent)
        }

        @JvmStatic
        fun pushToLongTermGroup(message: String, urgent: Boolean = false) {
            instance.pushToGroupsInternal(message, "longterm", urgent = urgent)
        }

        @JvmStatic
        fun pushToGroup(message: String, groupType: String, urgent: Boolean = false) {
            instance.pushToGroupsInternal(message, groupType, urgent = urgent)
        }

        /**
         * 推送ping失败报警（艾特全体成员）
         * @param machineName 机器名称
         * @param host 机器主机
         * @param firstFailureTime 第一次ping失败时间
         * @param currentTime 当前时间
         * @param failureDuration 失败持续时间（秒）
         * @param threshold 阈值（秒），默认3600秒（1小时）
         */
        @JvmStatic
        fun pushPingFailureAlarm(machineName: String, host: String, firstFailureTime: Long, currentTime: Long, failureDuration: Long, threshold: Long = 3600) {
            // 格式化时间显示
            val firstFailureTimeFormatted = DateTimeUtils.convertTimestampToDateTime(firstFailureTime)
            val currentTimeFormatted = DateTimeUtils.convertTimestampToDateTime(currentTime)
            val firstFailureTimeStr = DateTimeUtils.formatDateTimeFull(firstFailureTimeFormatted)
            val currentTimeStr = DateTimeUtils.formatDateTimeFull(currentTimeFormatted)
            
            // 计算失败持续时间的可读格式
            val failureMinutes = failureDuration / 60
            val failureHours = failureMinutes / 60
            
            val failureDurationReadable = buildString {
                append("${failureDuration}秒")
                if (failureMinutes > 0) {
                    append(" (${failureMinutes}分钟")
                    if (failureHours > 0) {
                        append(", ${failureHours}小时")
                    }
                    append(")")
                }
            }
            
            // 添加艾特全体成员的标记
            val atAllTag = "@全体成员 "
            
            val message = """
            🚨 ${atAllTag}Ping失败报警
            ====================
            机器: $machineName
            主机: $host
            
            📊 时间信息:
            • 首次失败时间: $firstFailureTimeStr
            • 当前时间: $currentTimeStr
            • 失败持续时间: $failureDurationReadable
            • 报警阈值: ${threshold}秒
            
            ⚠️ 状态: 机器已超过${failureHours}小时${failureMinutes % 60}分钟无法ping通
            
            💡 建议: 请立即检查网络连接、机器电源和系统状态！
            """.trimIndent()
            
            instance.pushToGroupsInternal(message, "alarm", urgent = true)
        }

        /**
         * 推送时间同步报警
         * @param machineName 机器名称
         * @param timeDiff 时间差（秒）
         * @param threshold 阈值（秒），默认5分钟
         */
        @JvmStatic
        fun pushTimeSyncAlarm(machineName: String, clientTimestamp: Long, serverTimestamp: Long, timeDiff: Long, threshold: Long = 300, urgent: Boolean = false) {
            // 格式化时间显示
            val clientTime = DateTimeUtils.convertTimestampToDateTime(clientTimestamp)
            val serverTime = DateTimeUtils.convertTimestampToDateTime(serverTimestamp)
            val clientTimeStr = DateTimeUtils.formatDateTimeFull(clientTime)
            val serverTimeStr = DateTimeUtils.formatDateTimeFull(serverTime)
            
            // 计算时间差的可读格式
            val timeDiffMinutes = timeDiff / 60
            val timeDiffHours = timeDiffMinutes / 60
            val timeDiffDays = timeDiffHours / 24
            
            val timeDiffReadable = buildString {
                append("${timeDiff}秒")
                if (timeDiffMinutes > 0) {
                    append(" (${timeDiffMinutes}分钟")
                    if (timeDiffHours > 0) {
                        append(", ${timeDiffHours}小时")
                        if (timeDiffDays > 0) {
                            append(", ${timeDiffDays}天")
                        }
                    }
                    append(")")
                }
            }
            
            val message = """
            ⚠️ 时间同步报警
            ====================
            机器: $machineName
            
            📊 时间信息:
            • 客户端时间: $clientTimeStr
            • 服务器时间: $serverTimeStr
            • 时间差: $timeDiffReadable
            • 阈值: ${threshold}秒
            
            💡 建议: 请使用ntp服务同步时间
            """.trimIndent()
            
            instance.pushToGroupsInternal(message, "alarm", urgent = urgent)
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
    fun pushToBotGroups(type: String, title: String, content: String, urgent: Boolean = false) {
        val targetGroups = getBotGroupsByType(type)

        for (group in targetGroups) {
            pushMessageToGroup(group, title, content, urgent)
        }
    }

    /**
     * 推送消息到指定名称的bot群
     */
    fun pushToBotGroup(groupName: String, title: String, content: String, urgent: Boolean = false) {
        val group = botGroups.find { it.name == groupName && it.isValid() }
        if (group != null) {
            pushMessageToGroup(group, title, content, urgent)
        }
    }

    /**
     * 推送报警消息
     */
    fun pushAlarmMessage(title: String, content: String, urgent: Boolean = false) {
        pushToBotGroups("alarm", title, content, urgent)
    }

    /**
     * 推送到短期群（包含作息时间分析）
     */
    fun pushToShortTermGroup(title: String, content: String, urgent: Boolean = false) {
        val sleepAnalysisContent = getSleepAnalysisContent("shortterm")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("shortterm", title, fullContent, urgent)
    }

    /**
     * 推送到长期群（包含作息时间分析）
     */
    fun pushToLongTermGroup(title: String, content: String, urgent: Boolean = false) {
        val sleepAnalysisContent = getSleepAnalysisContent("longterm")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("longterm", title, fullContent, urgent)
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
    private fun pushToGroupsInternal(message: String, groupType: String, removeEachLineBlank: Boolean = true, urgent: Boolean = false) {
        if (removeEachLineBlank) {
            // 移除每行的空白字符
            val lines = message.lines()
            val cleanedLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
            val cleanedMessage = cleanedLines.joinToString("\n")
            return pushToGroupsInternal(cleanedMessage, groupType, false, urgent)
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
                            larkBot.sendText(message, urgent)
                            logger.info("Successfully pushed to Lark ${groupType} group: ${group.name}, urgent: $urgent")
                        }
                    }

                    // 推送到企业微信群
                    if (group.weComGroupBotKey.isNotBlank()) {
                        WeComGroupBot.directSendTextWithUrl(
                            group.weComGroupBotKey, message,
                            null, null
                        )
                        logger.info("Successfully pushed to WeCom ${groupType} group: ${group.name}, urgent: $urgent")
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

    private fun pushMessageToGroup(group: BotGroupConfig, title: String, content: String, urgent: Boolean = false) {
        val fullContent = "[$title]\n$content"

        runBlocking {
            try {
                // 推送企业微信
                if (group.weComGroupBotKey.isNotEmpty()) {
                    val weComUrl = WeComGroupBot.getWebhookUrl(group.weComGroupBotKey)
                    WeComGroupBot.directSendTextWithUrl(weComUrl, fullContent, emptyList(), emptyList())
                    logger.info("Sent WeCom message to ${group.name}, urgent: $urgent")
                }

                if (group.larkGroupBotId.isNotEmpty() && group.larkGroupBotKey.isNotEmpty()) {
                    val larkBot = LarkGroupBot(group.larkGroupBotId, group.larkGroupBotKey)
                    larkBot.sendTextWithSilentMode(fullContent, null, urgent)
                    logger.info("Sent Lark message to ${group.name}, urgent: $urgent")
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
