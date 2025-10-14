package com.khm.group.center.service

import com.khm.group.center.config.HeartbeatConfig
import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.time.DateTimeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

/**
 * 机器状态管理服务
 * 负责管理机器的ping状态和agent心跳状态
 */
@Service
@Slf4jKt
class MachineStatusService {

    @Autowired
    private lateinit var heartbeatConfig: HeartbeatConfig

    // 存储机器状态信息
    private val machineStatusMap = ConcurrentHashMap<String, MachineStatus>()

    /**
     * 机器状态信息
     */
    data class MachineStatus(
        var lastPingTime: Long? = null,  // 最后一次成功ping的时间戳
        var lastHeartbeatTime: Long? = null,  // 最后一次agent心跳时间戳
        var pingStatus: Boolean = false,  // 当前ping状态
        var agentStatus: Boolean = false,  // agent在线状态
        var lastPingError: String? = null  // 最后一次ping错误信息
    )

    /**
     * 初始化机器状态
     */
    fun initializeMachineStatus() {
        MachineConfig.machineList.forEach { machine ->
            machineStatusMap[machine.nameEng] = MachineStatus()
        }
        logger.info("Machine status initialization completed, total ${MachineConfig.machineList.size} machines")
    }

    /**
     * 对指定机器进行ping检测
     */
    suspend fun pingMachine(machine: MachineConfig): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(machine.host)
                val isReachable = address.isReachable(5000) // 5秒超时

                val currentTime = DateTimeUtils.getCurrentTimestamp()
                val status = machineStatusMap.getOrPut(machine.nameEng) { MachineStatus() }
                
                if (isReachable) {
                    status.lastPingTime = currentTime
                    status.pingStatus = true
                    status.lastPingError = null
                } else {
                    status.pingStatus = false
                    status.lastPingError = "Ping timeout"
                    logger.debug("Ping failed: ${machine.nameEng} (${machine.host})")
                }

                // 更新MachineConfig中的状态
                machine.lastPingTime = status.lastPingTime
                machine.pingStatus = status.pingStatus

                isReachable
            } catch (e: Exception) {
                val status = machineStatusMap.getOrPut(machine.nameEng) { MachineStatus() }
                status.pingStatus = false
                status.lastPingError = e.message ?: "Unknown error"
                
                // 更新MachineConfig中的状态
                machine.pingStatus = false

                logger.debug("Ping exception: ${machine.nameEng} (${machine.host}) - ${e.message}")
                false
            }
        }
    }

    /**
     * 处理agent心跳
     */
    fun processHeartbeat(serverNameEng: String, timestamp: Long): Boolean {
        val machine = MachineConfig.getMachineByNameEng(serverNameEng)
        if (machine == null) {
            logger.error("Received heartbeat from unknown machine: $serverNameEng")
            return false
        }

        val currentTime = DateTimeUtils.getCurrentTimestamp()
        
        // 检测时间戳单位并转换为秒级
        // 客户端发送的是毫秒级时间戳，服务器端使用秒级时间戳
        val clientTimestampSeconds = if (timestamp > 1_000_000_000_000L) {
            // 如果时间戳大于这个值，说明是毫秒级，需要转换为秒级
            timestamp / 1000
        } else {
            // 否则认为是秒级时间戳
            timestamp
        }
        
        val timeDiff = kotlin.math.abs(currentTime - clientTimestampSeconds)
        
        // 时间戳验证：如果时间相差超过配置阈值，记录警告并推送报警
        if (timeDiff > heartbeatConfig.timeSyncThreshold) {
            // 计算可读的时间差
            val timeDiffMinutes = timeDiff / 60
            val timeDiffHours = timeDiffMinutes / 60
            val timeDiffDays = timeDiffHours / 24
            
            logger.warn("Machine ${machine.nameEng} timestamp difference is large: ${timeDiff} seconds (${timeDiffMinutes} minutes, ${timeDiffHours} hours, ${timeDiffDays} days), may need time synchronization")
            logger.info("Client timestamp: $clientTimestampSeconds, Server timestamp: $currentTime, Time difference: $timeDiff seconds")
            
            // 推送时间同步报警到报警群（默认不紧急）
            BotPushService.pushTimeSyncAlarm(
                machine.nameEng,
                clientTimestampSeconds,
                currentTime,
                timeDiff,
                heartbeatConfig.timeSyncThreshold.toLong(),
                urgent = false
            )
        }

        val status = machineStatusMap.getOrPut(serverNameEng) { MachineStatus() }
        status.lastHeartbeatTime = currentTime
        status.agentStatus = true

        // 更新MachineConfig中的状态
        machine.lastHeartbeatTime = currentTime
        machine.agentStatus = true

        logger.info("Received heartbeat from machine ${machine.nameEng}, timestamp difference: ${timeDiff} seconds")
        return true
    }

    /**
     * 获取所有机器状态
     */
    fun getAllMachineStatus(): Map<String, MachineStatus> {
        return machineStatusMap.toMap()
    }

    /**
     * 获取指定机器状态
     */
    fun getMachineStatus(serverNameEng: String): MachineStatus? {
        return machineStatusMap[serverNameEng]
    }

    /**
     * 检查agent是否在线（根据配置的时间间隔）
     */
    fun isAgentOnline(serverNameEng: String): Boolean {
        val status = machineStatusMap[serverNameEng] ?: return false
        val lastHeartbeat = status.lastHeartbeatTime ?: return false
        
        val currentTime = DateTimeUtils.getCurrentTimestamp()
        return (currentTime - lastHeartbeat) <= heartbeatConfig.onlineCheckInterval
    }

    /**
     * 检查机器是否可ping通（最近10分钟内成功ping）
     */
    fun isMachineReachable(serverNameEng: String): Boolean {
        val status = machineStatusMap[serverNameEng] ?: return false
        val lastPing = status.lastPingTime ?: return false
        
        val currentTime = DateTimeUtils.getCurrentTimestamp()
        return (currentTime - lastPing) <= 600 // 10分钟
    }

    /**
     * 清理过期状态（超过配置时间无心跳的机器标记为离线并推送报警）
     */
    fun cleanupExpiredStatus() {
        val currentTime = DateTimeUtils.getCurrentTimestamp()
        val expiredMachines = mutableListOf<String>()
        val offlineMachines = mutableListOf<String>()

        machineStatusMap.forEach { (nameEng, status) ->
            val lastHeartbeat = status.lastHeartbeatTime
            if (lastHeartbeat != null) {
                val timeDiff = currentTime - lastHeartbeat
                
                if (timeDiff > heartbeatConfig.offlineTimeout) {
                    status.agentStatus = false
                    expiredMachines.add(nameEng)
                    
                    // 更新MachineConfig中的状态
                    val machine = MachineConfig.getMachineByNameEng(nameEng)
                    machine?.agentStatus = false
                    
                    // 推送到报警群
                    offlineMachines.add(nameEng)
                }
            }
        }

        if (expiredMachines.isNotEmpty()) {
            logger.info("Marked the following machines as offline: ${expiredMachines.joinToString()}")
            
            // 推送离线报警到报警群
            if (offlineMachines.isNotEmpty()) {
                val timeoutMinutes = heartbeatConfig.offlineTimeout / 60
                val offlineMessage = """
                🚨 机器离线报警
                ====================
                以下机器超过${timeoutMinutes}分钟无心跳，已标记为离线：
                ${offlineMachines.joinToString(", ")}
                
                请及时检查机器状态和网络连接！
                """.trimIndent()
                BotPushService.pushToAlarmGroup(offlineMessage)
            }
        }
    }
}