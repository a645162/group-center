package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.webhook.AllWebHookServer

class MachineConfig : MachineBaseConfig() {
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
