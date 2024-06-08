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
            data class WeCom(
                val userId: String,
                val userMobilePhone: String
            )

            @Serializable
            data class Lark(
                val userId: String,
                val userMobilePhone: String
            )

            @Serializable
            data class Webhook(
                val lark: Lark,
                val weCom: WeCom
            )

            @Serializable
            data class User(
                val name: String,
                val nameEng: String,
                val keywords: List<String>,
                val year: Int,
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
                newUserObj.year = currentOriUser.year

                newUserObj.lark.userId =
                    currentOriUser.webhook.lark.userId
                newUserObj.lark.userMobilePhone =
                    currentOriUser.webhook.lark.userMobilePhone

                newUserObj.weCom.userId =
                    currentOriUser.webhook.weCom.userId
                newUserObj.weCom.userMobilePhone =
                    currentOriUser.webhook.weCom.userMobilePhone

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
