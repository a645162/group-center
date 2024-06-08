package com.khm.group.center.config.feature

import com.charleskorn.kaml.Yaml
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.utils.file.ProgramFile
import kotlinx.serialization.Serializable

class MachineConfigParser {
    companion object {
        fun readMachineYamlFile() {
            val dirPath = ConfigEnvironment.getEnvStr(
                "CONFIG_MACHINE_DIR_PATH"
            )
            if (dirPath.isNotEmpty()) {
                val machineList = parseMachineYamlInDir(dirPath)
                MachineConfig.machineList = machineList
                return
            }
            val path = ConfigEnvironment.getEnvStr(
                "CONFIG_MACHINE_PATH",
                ""
            )
            if (path.isEmpty()) {
                return
            }
            val machineList = parseMachineYamlInDir(path)
            MachineConfig.machineList = machineList
        }

        fun parseMachineYaml(yamlText: String): List<MachineConfig> {
            val machineList = mutableListOf<MachineConfig>()

            @Serializable
            data class SilentMode(
                val enable: Boolean,
                val startTime: String,
                val endTime: String
            )

            @Serializable
            data class WeCom(
                val enable: Boolean,
                val groupBotKey: String,
            )

            @Serializable
            data class Lark(
                val enable: Boolean,
                val groupBotId: String,
                val groupBotKey: String,
            )

            @Serializable
            data class Webhook(
                val silentMode: SilentMode,
                val lark: Lark,
                val weCom: WeCom
            )

            @Serializable
            data class Machine(
                val name: String,

                val nameEng: String,
                val host: String,

                val password: String,

                val webhook: Webhook
            )

            @Serializable
            data class MachineYamlFile(
                val version: Int,
                val enable: Boolean,
                val machineList: List<Machine>
            )

            val machineListTemp = Yaml.default.decodeFromString(
                MachineYamlFile.serializer(),
                yamlText
            )

            if (!machineListTemp.enable) {
                return machineList
            }

            for (machine in machineListTemp.machineList) {
                val newMachineObj = MachineConfig()

                val currentOriMachine = machine

                newMachineObj.name = currentOriMachine.name
                newMachineObj.nameEng = currentOriMachine.nameEng
                newMachineObj.host = currentOriMachine.host
                newMachineObj.password = currentOriMachine.password

                newMachineObj.silentMode.enable = currentOriMachine.webhook.silentMode.enable
                newMachineObj.silentMode.startTime = currentOriMachine.webhook.silentMode.startTime
                newMachineObj.silentMode.endTime = currentOriMachine.webhook.silentMode.endTime
                newMachineObj.silentMode.parse()

                newMachineObj.weComServer.enable = currentOriMachine.webhook.weCom.enable
                newMachineObj.weComServer.groupBotKey = currentOriMachine.webhook.weCom.groupBotKey

                newMachineObj.larkServer.enable = currentOriMachine.webhook.lark.enable
                newMachineObj.larkServer.groupBotId = currentOriMachine.webhook.lark.groupBotId
                newMachineObj.larkServer.groupBotKey = currentOriMachine.webhook.lark.groupBotKey

                machineList.add(newMachineObj)
            }

            return machineList
        }

        fun parseMachineYamlInDir(dirPath: String): List<MachineConfig> {
            val machineList = mutableListOf<MachineConfig>()

            val yamlPathList = ProgramFile.walkFileTreeKtRecursive(dirPath, "yaml")

            for (yamlFilePath in yamlPathList) {
                try {
                    val yamlText = ProgramFile.readFile(yamlFilePath)

                    val machineListTemp = parseMachineYaml(yamlText)

                    machineList.addAll(machineListTemp)
                } catch (e: Exception) {
                    println("Read $yamlFilePath Error: ${e.message}")
                }
            }

            return machineList
        }
    }
}
