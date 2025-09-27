package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.webhook.AllWebHookServer
import com.khm.group.center.datatype.response.FrontEndMachine

class MachineConfig : MachineBaseConfig() {
    var apiUrl: String = ""
    var apiKeyWords: List<String> = listOf()

    var password: String = ""

    var position: String = ""

    var isGpu: Boolean = false

    var webhook: AllWebHookServer = AllWebHookServer()

    // 保活相关字段
    var lastPingTime: Long? = null  // 最后一次成功ping的时间戳
    var lastHeartbeatTime: Long? = null  // 最后一次agent心跳时间戳
    var pingStatus: Boolean = false  // 当前ping状态
    var agentStatus: Boolean = false  // agent在线状态

    fun toFrontEndMachine(): FrontEndMachine {
        val frontEndMachine = FrontEndMachine(
            machineName = name,
            machineUrl = apiUrl,
            urlKeywords = apiKeyWords,
            position = position,
            isGpu = isGpu
        )

        return frontEndMachine
    }

    companion object {
        var machineList: List<MachineConfig> = listOf()

        fun getMachineByNameEng(nameEng: String): MachineConfig? {
            val finalNameEng = nameEng.trim()

            for (machine in machineList) {
                if (machine.nameEng == finalNameEng) {
                    return machine
                }
            }

            return null
        }
    }
}
