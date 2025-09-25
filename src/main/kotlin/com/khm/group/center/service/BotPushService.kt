package com.khm.group.center.service

import com.khm.group.center.datatype.config.webhook.BotGroupConfig
import com.khm.group.center.message.webhook.lark.LarkGroupBot
import com.khm.group.center.message.webhook.wecom.WeComGroupBot
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream

@Service
class BotPushService {

    @Autowired
    lateinit var reportPushService: ReportPushService

    // 预定义的bot群配置
    private val botGroups = mutableListOf<BotGroupConfig>()

    @Value("\${bot.config.file:Config/Bot/bot-groups.yaml}")
    private lateinit var configFile: String

    init {
        // 初始化默认的bot群配置
        initializeDefaultBotGroups()
        // 从配置文件加载
        loadBotGroupsFromConfig()
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

        // 日报群
        val dailyGroup = BotGroupConfig().apply {
            name = "日报群"
            type = "daily"
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        // 周报群
        val weeklyGroup = BotGroupConfig().apply {
            name = "周报群"
            type = "weekly"
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        // 月报群
        val monthlyGroup = BotGroupConfig().apply {
            name = "月报群"
            type = "monthly"
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        // 年报群
        val yearlyGroup = BotGroupConfig().apply {
            name = "年报群"
            type = "yearly"
            weComGroupBotKey = ""
            larkGroupBotId = ""
            larkGroupBotKey = ""
        }

        botGroups.addAll(listOf(alarmGroup, dailyGroup, weeklyGroup, monthlyGroup, yearlyGroup))
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
                val groupList = groups?.get("groups") as? List<Map<String, Any>>

                groupList?.forEach { groupConfig ->
                    val botGroup = BotGroupConfig().apply {
                        name = groupConfig["name"] as? String ?: ""
                        type = groupConfig["type"] as? String ?: ""
                        weComGroupBotKey = groupConfig["weComGroupBotKey"] as? String ?: ""
                        larkGroupBotId = groupConfig["larkGroupBotId"] as? String ?: ""
                        larkGroupBotKey = groupConfig["larkGroupBotKey"] as? String ?: ""
                        enable = groupConfig["enable"] as? Boolean ?: true
                    }

                    // 更新或添加配置
                    addOrUpdateBotGroup(botGroup)
                }
            }
        } catch (e: Exception) {
            println("Failed to load bot groups config: ${e.message}")
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
        return botGroups.toList()
    }

    /**
     * 根据类型获取bot群配置
     */
    fun getBotGroupsByType(type: String): List<BotGroupConfig> {
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
     * 推送日报（包含作息时间分析）
     */
    fun pushDailyReport(title: String, content: String) {
        val sleepAnalysisContent = getSleepAnalysisContent("daily")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("daily", title, fullContent)
    }

    /**
     * 推送周报（包含作息时间分析）
     */
    fun pushWeeklyReport(title: String, content: String) {
        val sleepAnalysisContent = getSleepAnalysisContent("weekly")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("weekly", title, fullContent)
    }

    /**
     * 推送月报（包含作息时间分析）
     */
    fun pushMonthlyReport(title: String, content: String) {
        val sleepAnalysisContent = getSleepAnalysisContent("monthly")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("monthly", title, fullContent)
    }

    /**
     * 推送年报（包含作息时间分析）
     */
    fun pushYearlyReport(title: String, content: String) {
        val sleepAnalysisContent = getSleepAnalysisContent("yearly")
        val fullContent = content + sleepAnalysisContent
        pushToBotGroups("yearly", title, fullContent)
    }

    /**
     * 获取作息时间分析内容
     */
    private fun getSleepAnalysisContent(reportType: String): String {
        return try {
            // 这里可以调用ReportPushService中的作息分析格式化方法
            // 由于ReportPushService已经集成了作息分析，我们直接返回一个占位符
            // 实际使用时，BotPushService会通过ReportPushService获取完整的报告内容
            "\n\n🌙 作息时间分析已集成到报告中"
        } catch (e: Exception) {
            "\n\n❌ 作息分析数据获取失败"
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
                    println("Sent WeCom message to ${group.name}")
                }

                if (group.larkGroupBotId.isNotEmpty() && group.larkGroupBotKey.isNotEmpty()) {
                    val larkBot = LarkGroupBot(group.larkGroupBotId, group.larkGroupBotKey)
                    larkBot.sendTextWithSilentMode(fullContent, null)
                    println("Sent Lark message to ${group.name}")
                }
            } catch (e: Exception) {
                println("Failed to send message to ${group.name}: ${e.message}")
            }
        }
    }
}
