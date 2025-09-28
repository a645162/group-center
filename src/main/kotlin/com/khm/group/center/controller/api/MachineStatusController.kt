package com.khm.group.center.controller.api

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.service.MachineStatusService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import com.khm.group.center.utils.time.DateTimeUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * 机器状态查询控制器
 * 提供机器ping状态和agent心跳状态的查询接口
 */
@RestController
@Tag(name = "Machine Status", description = "Machine ping status and agent heartbeat status query API")
@Slf4jKt
class MachineStatusController {

    @Autowired
    lateinit var machineStatusService: MachineStatusService

    /**
     * 机器状态响应数据
     */
    data class MachineStatusResponse(
        val name: String,
        val nameEng: String,
        val host: String,
        val position: String,
        val isGpu: Boolean,
        val pingStatus: Boolean,
        val agentStatus: Boolean,
        val lastPingTime: Long?,
        val lastHeartbeatTime: Long?,
        val lastPingTimeFormatted: String?,
        val lastHeartbeatTimeFormatted: String?,
        val pingStatusText: String,
        val agentStatusText: String
    )

    @Operation(
        summary = "Get All Machine Status",
        description = "Retrieve status information for all machines including ping status, agent status, and last update times"
    )
    @RequestMapping("/api/machine/status", method = [RequestMethod.GET])
    fun getAllMachineStatus(): List<MachineStatusResponse> {
        logger.debug("Query all machine status")

        return MachineConfig.machineList.map { machine ->
            val status = machineStatusService.getMachineStatus(machine.nameEng)
            
            MachineStatusResponse(
                name = machine.name,
                nameEng = machine.nameEng,
                host = machine.host,
                position = machine.position,
                isGpu = machine.isGpu,
                pingStatus = status?.pingStatus ?: false,
                agentStatus = status?.agentStatus ?: false,
                lastPingTime = status?.lastPingTime,
                lastHeartbeatTime = status?.lastHeartbeatTime,
                lastPingTimeFormatted = status?.lastPingTime?.let { 
                    DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(it)) 
                },
                lastHeartbeatTimeFormatted = status?.lastHeartbeatTime?.let { 
                    DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(it)) 
                },
                pingStatusText = if (status?.pingStatus == true) "在线" else "离线",
                agentStatusText = if (status?.agentStatus == true) "在线" else "离线"
            )
        }
    }

    @Operation(
        summary = "Get Specific Machine Status",
        description = "Retrieve detailed status information for a specific machine by its English name"
    )
    @RequestMapping("/api/machine/status/{nameEng}", method = [RequestMethod.GET])
    fun getMachineStatus(
        @Parameter(description = "English name of the machine")
        @PathVariable nameEng: String
    ): MachineStatusResponse? {
        logger.debug("Query machine status: $nameEng")

        val machine = MachineConfig.getMachineByNameEng(nameEng)
        if (machine == null) {
            logger.warn("Query unknown machine status: $nameEng")
            return null
        }

        val status = machineStatusService.getMachineStatus(nameEng)
        
        return MachineStatusResponse(
            name = machine.name,
            nameEng = machine.nameEng,
            host = machine.host,
            position = machine.position,
            isGpu = machine.isGpu,
            pingStatus = status?.pingStatus ?: false,
            agentStatus = status?.agentStatus ?: false,
            lastPingTime = status?.lastPingTime,
            lastHeartbeatTime = status?.lastHeartbeatTime,
            lastPingTimeFormatted = status?.lastPingTime?.let { 
                DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(it)) 
            },
            lastHeartbeatTimeFormatted = status?.lastHeartbeatTime?.let { 
                DateTimeUtils.formatDateTimeFull(DateTimeUtils.convertTimestampToDateTime(it)) 
            },
            pingStatusText = if (status?.pingStatus == true) "在线" else "离线",
            agentStatusText = if (status?.agentStatus == true) "在线" else "离线"
        )
    }

    @Operation(
        summary = "Get Machine Status Summary",
        description = "Retrieve summary statistics for all machines including online counts and availability rates"
    )
    @RequestMapping("/api/machine/status/summary", method = [RequestMethod.GET])
    fun getMachineStatusSummary(): Map<String, Any> {
        logger.debug("Query machine status statistics")

        val totalMachines = MachineConfig.machineList.size
        var onlinePingCount = 0
        var onlineAgentCount = 0

        MachineConfig.machineList.forEach { machine ->
            val status = machineStatusService.getMachineStatus(machine.nameEng)
            if (status?.pingStatus == true) onlinePingCount++
            if (status?.agentStatus == true) onlineAgentCount++
        }

        return mapOf(
            "totalMachines" to totalMachines,
            "onlinePingCount" to onlinePingCount,
            "onlineAgentCount" to onlineAgentCount,
            "pingOnlineRate" to if (totalMachines > 0) "%.2f".format(onlinePingCount.toDouble() / totalMachines * 100) + "%" else "0%",
            "agentOnlineRate" to if (totalMachines > 0) "%.2f".format(onlineAgentCount.toDouble() / totalMachines * 100) + "%" else "0%",
            "lastUpdateTime" to DateTimeUtils.getCurrentTimestamp()
        )
    }
}