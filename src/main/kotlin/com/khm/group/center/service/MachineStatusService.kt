package com.khm.group.center.service

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.time.DateTimeUtils
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
        logger.info("初始化机器状态完成，共 ${MachineConfig.machineList.size} 台机器")
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
                    logger.debug("Ping成功: ${machine.nameEng} (${machine.host})")
                } else {
                    status.pingStatus = false
                    status.lastPingError = "Ping超时"
                    logger.warn("Ping失败: ${machine.nameEng} (${machine.host})")
                }

                // 更新MachineConfig中的状态
                machine.lastPingTime = status.lastPingTime
                machine.pingStatus = status.pingStatus

                isReachable
            } catch (e: Exception) {
                val status = machineStatusMap.getOrPut(machine.nameEng) { MachineStatus() }
                status.pingStatus = false
                status.lastPingError = e.message ?: "未知错误"
                
                // 更新MachineConfig中的状态
                machine.pingStatus = false

                logger.error("Ping异常: ${machine.nameEng} (${machine.host}) - ${e.message}")
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
            logger.error("收到未知机器的心跳: $serverNameEng")
            return false
        }

        val currentTime = DateTimeUtils.getCurrentTimestamp()
        val timeDiff = kotlin.math.abs(currentTime - timestamp)
        
        // 时间戳验证：如果时间相差超过5分钟，记录警告
        if (timeDiff > 300) { // 5分钟 = 300秒
            logger.warn("机器 ${machine.nameEng} 时间戳差异较大: ${timeDiff}秒，可能需要同步时间")
        }

        val status = machineStatusMap.getOrPut(serverNameEng) { MachineStatus() }
        status.lastHeartbeatTime = currentTime
        status.agentStatus = true

        // 更新MachineConfig中的状态
        machine.lastHeartbeatTime = currentTime
        machine.agentStatus = true

        logger.info("收到机器 ${machine.nameEng} 心跳，时间戳差异: ${timeDiff}秒")
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
     * 检查agent是否在线（最近5分钟内有心跳）
     */
    fun isAgentOnline(serverNameEng: String): Boolean {
        val status = machineStatusMap[serverNameEng] ?: return false
        val lastHeartbeat = status.lastHeartbeatTime ?: return false
        
        val currentTime = DateTimeUtils.getCurrentTimestamp()
        return (currentTime - lastHeartbeat) <= 300 // 5分钟
    }

    /**
     * 检查机器是否可ping通（最近2分钟内成功ping）
     */
    fun isMachineReachable(serverNameEng: String): Boolean {
        val status = machineStatusMap[serverNameEng] ?: return false
        val lastPing = status.lastPingTime ?: return false
        
        val currentTime = DateTimeUtils.getCurrentTimestamp()
        return (currentTime - lastPing) <= 120 // 2分钟
    }

    /**
     * 清理过期状态（超过1小时无心跳的机器标记为离线）
     */
    fun cleanupExpiredStatus() {
        val currentTime = DateTimeUtils.getCurrentTimestamp()
        val expiredMachines = mutableListOf<String>()

        machineStatusMap.forEach { (nameEng, status) ->
            val lastHeartbeat = status.lastHeartbeatTime
            if (lastHeartbeat != null && (currentTime - lastHeartbeat) > 3600) { // 1小时
                status.agentStatus = false
                expiredMachines.add(nameEng)
                
                // 更新MachineConfig中的状态
                val machine = MachineConfig.getMachineByNameEng(nameEng)
                machine?.agentStatus = false
            }
        }

        if (expiredMachines.isNotEmpty()) {
            logger.info("标记以下机器为离线状态: ${expiredMachines.joinToString()}")
        }
    }
}