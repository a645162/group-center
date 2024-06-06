package com.khm.group.center.datatype.config

import com.khm.group.center.datatype.config.webhook.LarkServer
import com.khm.group.center.datatype.config.webhook.WeComServer

class MachineConfig {
    var name: String = ""

    var nameEng: String = ""
    var host: String = ""

    var password: String = ""

    var weComServer = WeComServer()
    var larkServer = LarkServer()

    companion object{
        var machineList: List<MachineConfig> = listOf()
    }
}
