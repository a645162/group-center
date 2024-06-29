package com.khm.group.center.config.feature

import com.charleskorn.kaml.Yaml
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.utils.file.ProgramFile
import kotlinx.serialization.Serializable

class GroupUserConfigParser {
    companion object {
        fun readUserYamlFile() {
            val dirPath = ConfigEnvironment.getEnvStr(
                "CONFIG_USER_DIR_PATH"
            )
            if (dirPath.isNotEmpty()) {
                val userList = parseUserYamlInDir(dirPath)
                GroupUserConfig.userList = userList
                return
            }
            val path = ConfigEnvironment.getEnvStr(
                "CONFIG_USER_PATH",
                ""
            )
            if (path.isEmpty()) {
                return
            }
            val userList = parseUserYaml(path)
            if (path.isEmpty()) {
                return
            }
            GroupUserConfig.userList = userList
        }

        fun parseUserYaml(yamlText: String): List<GroupUserConfig> {
            val userList = mutableListOf<GroupUserConfig>()

            @Serializable
            data class SilentMode(
                val enable: Boolean,
                val startTime: String,
                val endTime: String
            )

            @Serializable
            data class WeCom(
                val enable: Boolean,
                val userId: String,
                val userMobilePhone: String
            )

            @Serializable
            data class Lark(
                val enable: Boolean,
                val userId: String,
                val userMobilePhone: String
            )

            @Serializable
            data class Webhook(
                val silentMode: SilentMode,
                val lark: Lark,
                val weCom: WeCom
            )

            @Serializable
            data class LinuxUser(
                val uid: Int,
                val gid: Int
            )

            @Serializable
            data class User(
                val name: String,
                val nameEng: String,
                val keywords: List<String>,
                val password: String,
                val year: Int,
                val linuxUser: LinuxUser,
                val webhook: Webhook
            )

            @Serializable
            data class UserYamlFile(
                val version: Int,
                val enable: Boolean,
                val userList: List<User>
            )

            val userListTemp = Yaml.default.decodeFromString(
                UserYamlFile.serializer(),
                yamlText
            )

            if (!userListTemp.enable) {
                return userList
            }

            for (user in userListTemp.userList) {
                val newUserObj = GroupUserConfig()

                val currentOriUser = user

                newUserObj.name = currentOriUser.name
                newUserObj.nameEng = currentOriUser.nameEng
                newUserObj.keywords = currentOriUser.keywords
                newUserObj.password = currentOriUser.password
                newUserObj.year = currentOriUser.year

                newUserObj.linuxUser.uid = currentOriUser.linuxUser.uid
                newUserObj.linuxUser.gid = currentOriUser.linuxUser.gid

                newUserObj.webhook.silentMode.startTime =
                    currentOriUser.webhook.silentMode.startTime
                newUserObj.webhook.silentMode.endTime =
                    currentOriUser.webhook.silentMode.endTime
                newUserObj.webhook.silentMode.parse()
                newUserObj.webhook.silentMode.enable =
                    currentOriUser.webhook.silentMode.enable

                newUserObj.webhook.lark.userId =
                    currentOriUser.webhook.lark.userId
                newUserObj.webhook.lark.userMobilePhone =
                    currentOriUser.webhook.lark.userMobilePhone
                newUserObj.webhook.lark.enable =
                    currentOriUser.webhook.lark.enable

                newUserObj.webhook.weCom.userId =
                    currentOriUser.webhook.weCom.userId
                newUserObj.webhook.weCom.userMobilePhone =
                    currentOriUser.webhook.weCom.userMobilePhone
                newUserObj.webhook.weCom.enable =
                    currentOriUser.webhook.weCom.enable

                userList.add(newUserObj)
            }

            return userList
        }

        fun parseUserYamlInDir(dirPath: String): List<GroupUserConfig> {
            val userList = mutableListOf<GroupUserConfig>()

            val yamlPathList = ProgramFile.walkFileTreeKtRecursive(dirPath, "yaml")

            for (yamlFilePath in yamlPathList) {
                try {
                    val yamlText = ProgramFile.readFile(yamlFilePath)

                    val userListTemp = parseUserYaml(yamlText)

                    userList.addAll(userListTemp)
                } catch (e: Exception) {
                    println("Read $yamlFilePath Error: ${e.message}")
                }
            }

            return userList
        }
    }
}
