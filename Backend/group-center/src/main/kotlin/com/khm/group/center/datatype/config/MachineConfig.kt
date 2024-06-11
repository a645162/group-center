package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.feature.SilentModeConfig
import com.khm.group.center.datatype.config.webhook.AllWebHookServer
import com.khm.group.center.datatype.config.webhook.BaseWebHookServer
import com.khm.group.center.datatype.config.webhook.LarkServer
import com.khm.group.center.datatype.config.webhook.WeComServer

class MachineConfig {
    var name: String = ""

    var nameEng: String = ""
    var host: String = ""

    var password: String = ""

    var webhook: AllWebHookServer = AllWebHookServer()

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
