package com.khm.group.center.datatype.config

open class MachineBaseConfig {

    var name: String = ""

    var nameEng: String = ""
    var host: String = ""

    fun copy(): MachineBaseConfig {
        val newConfig = MachineBaseConfig()

        newConfig.name = name
        newConfig.nameEng = nameEng
        newConfig.host = host

        return newConfig
    }

}
