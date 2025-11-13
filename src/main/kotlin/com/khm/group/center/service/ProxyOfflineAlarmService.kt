package com.khm.group.center.service

import com.khm.group.center.datatype.config.ProxyTestServer
import com.khm.group.center.datatype.config.ProxyStatus
import com.khm.group.center.datatype.config.ProxyConfigManager
import com.khm.group.center.datatype.config.AlarmConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.time.DateTimeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 代理离线报警服务
 * 负责检测代理服务器长时间离线并发送报警
 */
@Service
@Slf4jKt
class ProxyOfflineAlarmService {

    @Autowired
    private lateinit var botPushService: BotPushService

    /**
     * 检查所有代理服务器是否需要发送离线报警
     */
    fun checkAllProxyOfflineAlarms() {
        val enabledProxies = ProxyConfigManager.getEnabledProxyTests()
        
        if (enabledProxies.isEmpty()) {
            logger.debug("No enabled proxy test servers to check for offline alarms")
            return
        }

        logger.debug("Checking offline alarms for ${enabledProxies.size} proxy test servers")
        
        var alarmCount = 0
        enabledProxies.forEach { proxy ->
            try {
                if (checkAndSendProxyOfflineAlarm(proxy)) {
                    alarmCount++
                }
            } catch (e: Exception) {
                logger.error("Failed to check offline alarm for proxy ${proxy.nameEng}: ${e.message}", e)
            }
        }
        
        if (alarmCount > 0) {
            logger.info("Proxy offline alarm check completed: $alarmCount alarms sent")
        } else {
            logger.debug("Proxy offline alarm check completed: no alarms needed")
        }
    }

    /**
     * 检查单个代理服务器是否需要发送离线报警
     * @param proxy 代理服务器配置
     * @return 是否发送了报警
     */
    fun checkAndSendProxyOfflineAlarm(proxy: ProxyTestServer): Boolean {
        val status = ProxyConfigManager.proxyStatusMap[proxy.nameEng] ?: return false
        
        // 检查是否需要发送报警
        if (!status.shouldSendOfflineAlarm(proxy.testConfig.alarmConfig)) {
            return false
        }
        
        // 发送报警消息
        val success = sendProxyOfflineAlarm(proxy, status)
        if (success) {
            status.recordAlarmTime()
            logger.info("Proxy offline alarm sent successfully: ${proxy.nameEng}, offline duration: ${status.offlineDurationMinutes} minutes")
        } else {
            logger.error("Failed to send proxy offline alarm: ${proxy.nameEng}")
        }
        
        return success
    }

    /**
     * 发送代理离线报警消息
     * @param proxy 代理服务器配置
     * @param status 代理状态
     * @return 是否发送成功
     */
    private fun sendProxyOfflineAlarm(proxy: ProxyTestServer, status: ProxyStatus): Boolean {
        try {
            val alarmConfig = proxy.testConfig.alarmConfig
            val title = if (alarmConfig.urgent) "🚨 紧急：代理服务器离线" else "⚠️ 代理服务器离线"
            
            val message = formatProxyOfflineMessage(proxy, status, alarmConfig, title)
            
            // 使用BotPushService发送到报警群
            BotPushService.pushToAlarmGroup(message, urgent = alarmConfig.urgent)
            
            logger.info("Proxy offline alarm sent: ${proxy.nameEng}, urgent: ${alarmConfig.urgent}")
            return true
        } catch (e: Exception) {
            logger.error("Failed to send proxy offline alarm for ${proxy.nameEng}: ${e.message}", e)
            return false
        }
    }

    /**
     * 格式化代理离线报警消息
     * @param proxy 代理服务器配置
     * @param status 代理状态
     * @param alarmConfig 报警配置
     * @param title 消息标题
     * @return 格式化后的消息内容
     */
    private fun formatProxyOfflineMessage(
        proxy: ProxyTestServer,
        status: ProxyStatus,
        alarmConfig: AlarmConfig,
        title: String
    ): String {
        val lastSuccessTime = status.lastSuccessTime
        val lastCheckTime = status.lastCheckTime
        
        val lastSuccessTimeStr = if (lastSuccessTime != null) {
            DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(lastSuccessTime))
        } else {
            "从未成功"
        }
        
        val lastCheckTimeStr = if (lastCheckTime != null) {
            DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(lastCheckTime))
        } else {
            "未检查"
        }
        
        val offlineDurationHours = status.offlineDurationMinutes / 60
        val offlineDurationMinutes = status.offlineDurationMinutes % 60
        
        val timeoutHours = alarmConfig.offlineTimeoutMinutes / 60
        val timeoutMinutes = alarmConfig.offlineTimeoutMinutes % 60
        
        return buildString {
            append("$title\n")
            append("=".repeat(title.length))
            append("\n\n")
            
            append("📋 代理服务器信息:\n")
            append("• 名称: ${proxy.name} (${proxy.nameEng})\n")
            append("• 类型: ${proxy.getTypeString()}\n")
            append("• 地址: ${proxy.getProxyAddress()}\n")
            append("• 状态: 离线\n\n")
            
            append("📊 时间信息:\n")
            append("• 最后一次成功: $lastSuccessTimeStr\n")
            append("• 最后一次检查: $lastCheckTimeStr\n")
            append("• 离线持续时间: ${offlineDurationHours}小时${offlineDurationMinutes}分钟\n")
            append("• 报警阈值: ${timeoutHours}小时${timeoutMinutes}分钟\n\n")
            
            append("⚠️ 错误信息:\n")
            append("• ${status.lastError ?: "未知错误"}\n\n")
            
            append("💡 建议:\n")
            append("• 检查代理服务器网络连接\n")
            append("• 验证代理服务器配置\n")
            append("• 检查代理服务器服务状态\n")
            
            if (alarmConfig.urgent) {
                append("\n\n⚠️ 紧急报警，请立即处理！")
            }
        }
    }

    /**
     * 获取代理离线报警统计
     * @return 报警统计信息
     */
    fun getProxyOfflineAlarmStats(): ProxyOfflineAlarmStats {
        val enabledProxies = ProxyConfigManager.getEnabledProxyTests()
        val currentTime = System.currentTimeMillis()
        
        var totalAlarms = 0
        var pendingAlarms = 0
        val proxyAlarmDetails = mutableListOf<ProxyAlarmDetail>()
        
        enabledProxies.forEach { proxy ->
            val status = ProxyConfigManager.proxyStatusMap[proxy.nameEng] ?: return@forEach
            
            val alarmConfig = proxy.testConfig.alarmConfig
            val shouldAlarm = status.shouldSendOfflineAlarm(alarmConfig)
            
            if (shouldAlarm) {
                pendingAlarms++
            }
            
            if (status.lastAlarmTime != null) {
                totalAlarms++
            }
            
            proxyAlarmDetails.add(ProxyAlarmDetail(
                proxyName = proxy.name,
                proxyNameEng = proxy.nameEng,
                isAvailable = status.isAvailable,
                offlineDurationMinutes = status.offlineDurationMinutes,
                alarmEnabled = alarmConfig.enable,
                alarmTimeoutMinutes = alarmConfig.offlineTimeoutMinutes,
                shouldAlarm = shouldAlarm,
                lastAlarmTime = status.lastAlarmTime
            ))
        }
        
        return ProxyOfflineAlarmStats(
            totalProxies = enabledProxies.size,
            totalAlarms = totalAlarms,
            pendingAlarms = pendingAlarms,
            lastCheckTime = currentTime,
            proxyAlarmDetails = proxyAlarmDetails
        )
    }
}

/**
 * 代理离线报警统计信息
 */
data class ProxyOfflineAlarmStats(
    val totalProxies: Int,
    val totalAlarms: Int,
    val pendingAlarms: Int,
    val lastCheckTime: Long,
    val proxyAlarmDetails: List<ProxyAlarmDetail>
) {
    /**
     * 获取可读的统计描述
     */
    fun getStatsDescription(): String {
        return "Proxy offline alarm stats: $totalProxies proxies, $totalAlarms total alarms, $pendingAlarms pending alarms"
    }
}

/**
 * 单个代理报警详情
 */
data class ProxyAlarmDetail(
    val proxyName: String,
    val proxyNameEng: String,
    val isAvailable: Boolean,
    val offlineDurationMinutes: Long,
    val alarmEnabled: Boolean,
    val alarmTimeoutMinutes: Int,
    val shouldAlarm: Boolean,
    val lastAlarmTime: Long?
) {
    /**
     * 获取可读的报警状态
     */
    fun getAlarmStatus(): String {
        return when {
            !alarmEnabled -> "报警禁用"
            isAvailable -> "在线"
            shouldAlarm -> "需要报警"
            else -> "监控中"
        }
    }
    
    /**
     * 获取离线时间描述
     */
    fun getOfflineDurationDescription(): String {
        val hours = offlineDurationMinutes / 60
        val minutes = offlineDurationMinutes % 60
        return "${hours}小时${minutes}分钟"
    }
    
    /**
     * 获取报警阈值描述
     */
    fun getAlarmTimeoutDescription(): String {
        val hours = alarmTimeoutMinutes / 60
        val minutes = alarmTimeoutMinutes % 60
        return "${hours}小时${minutes}分钟"
    }
}