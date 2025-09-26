package com.khm.group.center.controller.api.client

import com.khm.group.center.datatype.receive.heartbeat.MachineHeartbeat
import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.service.MachineStatusService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Agent心跳控制器
 * 处理agent客户端发送的心跳包
 */
@RestController
@Slf4jKt
class HeartbeatController {

    @Autowired
    lateinit var machineStatusService: MachineStatusService

    @Operation(summary = "Agent心跳保活")
    @RequestMapping("/api/client/heartbeat", method = [RequestMethod.POST])
    fun processHeartbeat(@RequestBody heartbeat: MachineHeartbeat): ClientResponse {
        val responseObj = ClientResponse()

        try {
            // 验证时间戳有效性
            if (heartbeat.timestamp <= 0) {
                responseObj.result = "error: 无效的时间戳"
                responseObj.isSucceed = false
                responseObj.haveError = true
                logger.warn("收到无效时间戳的心跳: ${heartbeat.serverNameEng}, timestamp: ${heartbeat.timestamp}")
                return responseObj
            }

            // 处理心跳
            val success = machineStatusService.processHeartbeat(heartbeat.serverNameEng, heartbeat.timestamp)

            if (success) {
                responseObj.result = "success"
                responseObj.isSucceed = true
                responseObj.isAuthenticated = true
                logger.debug("处理心跳成功: ${heartbeat.serverNameEng}")
            } else {
                responseObj.result = "error: 未知机器"
                responseObj.isSucceed = false
                responseObj.haveError = true
                logger.warn("处理心跳失败: 未知机器 ${heartbeat.serverNameEng}")
            }
        } catch (e: Exception) {
            responseObj.result = "error: ${e.message}"
            responseObj.isSucceed = false
            responseObj.haveError = true
            logger.error("处理心跳时发生异常: ${heartbeat.serverNameEng} - ${e.message}")
        }

        return responseObj
    }
}