package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.feature.SilentModeConfig
import com.khm.group.center.datatype.config.webhook.BaseWebHookServer
import com.khm.group.center.datatype.config.webhook.LarkServer
import com.khm.group.center.datatype.config.webhook.WeComServer

class MachineConfig {
    var name: String = ""

    var nameEng: String = ""
    var host: String = ""

    var password: String = ""

    val silentMode: SilentModeConfig = SilentModeConfig()

    val weComServer = WeComServer()
    val larkServer = LarkServer()

    private var webhookServerList: List<BaseWebHookServer> = listOf(
        weComServer,
        larkServer
    )

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

    fun haveValidWebHookService(): Boolean {
        for (webhookServer in webhookServerList) {
            if (webhookServer.enable) {
                return true
            }
        }

        return false
    }
}
