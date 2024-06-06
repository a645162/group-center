package com.khm.group.center.config

import com.charleskorn.kaml.Yaml
import com.khm.group.center.config.GroupUserConfigParser.Companion.parseUserYaml
import com.khm.group.center.config.GroupUserConfigParser.Companion.parseUserYamlInDir
import com.khm.group.center.datatype.config.GroupUserConfig
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
            val machineList = parseMachineYamlInDir(path)
            if (path.isEmpty()) {
                return
            }
            MachineConfig.machineList = machineList
        }

        fun parseMachineYaml(yamlText: String): List<MachineConfig> {
            val machineList = mutableListOf<MachineConfig>()

            @Serializable
            data class WeCom(
                val enable: Boolean,
                val groupKey: String,
            )

            @Serializable
            data class Lark(
                val enable: Boolean,
                val groupKey: String,
            )

            @Serializable
            data class Webhook(
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
                val machineList: List<Machine>
            )

            val machineListTemp = Yaml.default.decodeFromString(
                MachineYamlFile.serializer(),
                yamlText
            )

            for (machine in machineListTemp.machineList) {
                val newMachineObj = MachineConfig()

                val currentOriMachine = machine

                newMachineObj.name = currentOriMachine.name
                newMachineObj.nameEng = currentOriMachine.nameEng
                newMachineObj.host = currentOriMachine.host
                newMachineObj.password = currentOriMachine.password

                newMachineObj.weComServer.enable = currentOriMachine.webhook.weCom.enable
                newMachineObj.weComServer.groupKey = currentOriMachine.webhook.weCom.groupKey

                newMachineObj.larkServer.enable = currentOriMachine.webhook.lark.enable
                newMachineObj.larkServer.groupKey = currentOriMachine.webhook.lark.groupKey

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