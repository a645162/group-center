package com.khm.group.center.service

import com.khm.group.center.config.HeartbeatConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * 报警推送服务（带间隔控制）
 * 提供统一的报警推送接口，支持按报警类型控制推送间隔
 */
@Service
@Slf4jKt
class AlarmPushService {

    @Autowired
    private lateinit var heartbeatConfig: HeartbeatConfig

    @Autowired
    private lateinit var botPushService: BotPushService

    // 记录每种报警类型的最后推送时间
    private val lastPushTimeMap = ConcurrentHashMap<String, Long>()

    /**
     * 推送报警消息（带间隔控制）
     * @param alarmType 报警类型（用于区分不同的报警，如：ping_failure, agent_offline, time_sync）
     * @param message 报警消息内容
     * @param urgent 是否紧急（紧急消息不受间隔限制）
     * @return 是否成功推送（如果被间隔限制返回false）
     */
    fun pushAlarmWithInterval(alarmType: String, message: String, urgent: Boolean = false): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // 紧急消息不受间隔限制
        if (!urgent) {
            val lastPushTime = lastPushTimeMap[alarmType]
            if (lastPushTime != null) {
                val timeDiff = currentTime - lastPushTime
                val intervalMs = heartbeatConfig.alarmPushInterval * 1000L
                
                if (timeDiff < intervalMs) {
                    val remainingMinutes = (intervalMs - timeDiff) / (60 * 1000)
                    logger.info("Alarm push skipped for type '$alarmType', next push in ${remainingMinutes} minutes")
                    return false
                }
            }
        }
        
        // 推送报警消息
        try {
            botPushService.pushAlarmMessage("🚨 系统报警", message, urgent)
            logger.info("Alarm pushed successfully for type: $alarmType, urgent: $urgent")
            
            // 更新最后推送时间
            lastPushTimeMap[alarmType] = currentTime
            return true
        } catch (e: Exception) {
            logger.error("Failed to push alarm for type '$alarmType': ${e.message}", e)
            return false
        }
    }

    /**
     * 推送ping失败报警（带间隔控制）
     * @param machineName 机器名称
     * @param host 机器主机
     * @param firstFailureTime 第一次ping失败时间
     * @param currentTime 当前时间
     * @param failureDuration 失败持续时间（秒）
     * @param threshold 阈值（秒）
     * @return 是否成功推送
     */
    fun pushPingFailureAlarm(
        machineName: String, 
        host: String, 
        firstFailureTime: Long, 
        currentTime: Long, 
        failureDuration: Long, 
        threshold: Long = 3600
    ): Boolean {
        val alarmType = "ping_failure_$machineName"
        
        // 格式化时间显示
        val firstFailureTimeFormatted = com.khm.group.center.utils.time.DateTimeUtils.convertTimestampToDateTime(firstFailureTime)
        val currentTimeFormatted = com.khm.group.center.utils.time.DateTimeUtils.convertTimestampToDateTime(currentTime)
        val firstFailureTimeStr = com.khm.group.center.utils.time.DateTimeUtils.formatDateTimeFull(firstFailureTimeFormatted)
        val currentTimeStr = com.khm.group.center.utils.time.DateTimeUtils.formatDateTimeFull(currentTimeFormatted)
        
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
        
        return pushAlarmWithInterval(alarmType, message, urgent = true)
    }

    /**
     * 推送agent离线报警（带间隔控制）
     * @param machineNames 离线机器名称列表
     * @param timeoutMinutes 超时时间（分钟）
     * @return 是否成功推送
     */
    fun pushAgentOfflineAlarm(machineNames: List<String>, timeoutMinutes: Int): Boolean {
        val alarmType = "agent_offline"
        
        val message = """
            🚨 机器离线报警
            ====================
            以下机器超过${timeoutMinutes}分钟无心跳，已标记为离线：
            ${machineNames.joinToString(", ")}
            
            请及时检查机器状态和网络连接！
            """.trimIndent()
        
        return pushAlarmWithInterval(alarmType, message, urgent = true)
    }

    /**
     * 推送时间同步报警（带间隔控制）
     * @param machineName 机器名称
     * @param clientTimestamp 客户端时间戳
     * @param serverTimestamp 服务器时间戳
     * @param timeDiff 时间差（秒）
     * @param threshold 阈值（秒）
     * @param urgent 是否紧急
     * @return 是否成功推送
     */
    fun pushTimeSyncAlarm(
        machineName: String,
        clientTimestamp: Long,
        serverTimestamp: Long,
        timeDiff: Long,
        threshold: Long = 300,
        urgent: Boolean = false
    ): Boolean {
        val alarmType = "time_sync_$machineName"
        
        // 格式化时间显示
        val clientTime = com.khm.group.center.utils.time.DateTimeUtils.convertTimestampToDateTime(clientTimestamp)
        val serverTime = com.khm.group.center.utils.time.DateTimeUtils.convertTimestampToDateTime(serverTimestamp)
        val clientTimeStr = com.khm.group.center.utils.time.DateTimeUtils.formatDateTimeFull(clientTime)
        val serverTimeStr = com.khm.group.center.utils.time.DateTimeUtils.formatDateTimeFull(serverTime)
        
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
        
        return pushAlarmWithInterval(alarmType, message, urgent)
    }

    /**
     * 清除指定报警类型的推送记录
     * @param alarmType 报警类型
     */
    fun clearAlarmRecord(alarmType: String) {
        lastPushTimeMap.remove(alarmType)
        logger.info("Alarm record cleared for type: $alarmType")
    }

    /**
     * 获取报警推送统计信息
     * @return 报警推送统计
     */
    fun getAlarmStats(): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val stats = mutableMapOf<String, Any>()
        
        lastPushTimeMap.forEach { (alarmType, lastPushTime) ->
            val timeDiff = currentTime - lastPushTime
            val timeDiffMinutes = timeDiff / (60 * 1000)
            stats[alarmType] = mapOf(
                "lastPushTime" to lastPushTime,
                "timeDiffMinutes" to timeDiffMinutes
            )
        }
        
        return stats
    }
}