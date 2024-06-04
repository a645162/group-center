package com.khm.group.center.config

import com.charleskorn.kaml.Yaml
import com.khm.group.center.datatype.config.UserConfig
import com.khm.group.center.utils.file.ProgramFile
import kotlinx.serialization.Serializable

class GroupUser {
    companion object {
        fun parseUserYaml(yamlText: String): List<UserConfig> {
            val userList = mutableListOf<UserConfig>()

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
                val userList: List<User>
            )

            val userListTemp = Yaml.default.decodeFromString(
                UserYamlFile.serializer(),
                yamlText
            )

            for (user in userListTemp.userList) {
                val newUserObj = UserConfig()

                val currentOriUser = user

                newUserObj.name = currentOriUser.name
                newUserObj.nameEng = currentOriUser.nameEng
                newUserObj.keywords = currentOriUser.keywords
                newUserObj.year = currentOriUser.year

                newUserObj.larkUser.userId =
                    currentOriUser.webhook.lark.userId
                newUserObj.larkUser.userMobilePhone =
                    currentOriUser.webhook.lark.userMobilePhone

                newUserObj.weComUser.userId =
                    currentOriUser.webhook.weCom.userId
                newUserObj.weComUser.userMobilePhone =
                    currentOriUser.webhook.weCom.userMobilePhone

                userList.add(newUserObj)
            }

            return userList
        }

        fun parseUserYamlInDir(dirPath: String): List<UserConfig> {
            val userList = mutableListOf<UserConfig>()

            val yamlPathList = ProgramFile.walkFileTreeKtRecursive(dirPath, "yaml")

            for (yamlFilePath in yamlPathList) {
                try {
                    val yamlText = ProgramFile.readFile(yamlFilePath)

                    val userListTemp = parseUserYaml(yamlText)

                    userList.addAll(userListTemp)
                } catch (e: Exception) {
                    println("Read ${yamlFilePath} Error: ${e.message}")
                }
            }

            return userList
        }
    }
}
