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
